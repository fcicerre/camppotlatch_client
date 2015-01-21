package org.coursera.camppotlatch.client.handler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.coursera.camppotlatch.client.commons.ImageUtils;
import org.coursera.camppotlatch.client.commons.UserUtils;
import org.coursera.camppotlatch.client.model.User;
import org.coursera.camppotlatch.client.model.UserImageType;
import org.coursera.camppotlatch.client.serviceproxy.PotlatchServiceConn;
import org.coursera.camppotlatch.client.serviceproxy.UserServiceApi;
import org.coursera.camppotlatch.client.view.LoginActivity;
import org.coursera.camppotlatch.client.view.UserListAdapter;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import retrofit.client.Response;

/**
 * Created by Fabio on 28/11/2014.
 */
public class UsersRefreshThread extends Thread {
    public interface OnLoadUsersListener {
        public List<User> onLoadUsers(String mSearchText, int offset, int limit);
    }

    private static final int PAGE_SIZE = 4;

    private static final String PAGE_EXTRA = "page";
    private static final String SEARCH_TEXT_EXTRA = "searchText";
    private static final String NEW_QUERY_EXTRA = "newQuery";

    private Activity mActivity;
    private ListView mUserListView;

    private Handler mRefreshHandler = null;
    private Semaphore mRefreshHandlerInit = new Semaphore(0, true);
    private Semaphore mRefreshSemaphore = new Semaphore(1, false);

    private int mIniPage = 0;
    private String mSearchText = null;
    private boolean mLastPageReached = false;

    private OnLoadUsersListener mOnLoadUsersListener;

    public UsersRefreshThread(Activity activity,
                              ListView userListView,
                              OnLoadUsersListener onLoadUsersListener) {
        mActivity = activity;
        mUserListView = userListView;
        mOnLoadUsersListener = onLoadUsersListener;
    }

    @Override
    public void run() {
        Looper.prepare();

        mRefreshHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Bundle bundle = msg.getData();
                int page = bundle.getInt(PAGE_EXTRA);
                String searchText = bundle.getString(SEARCH_TEXT_EXTRA);
                boolean newQuery = bundle.getBoolean(NEW_QUERY_EXTRA);

                List<User> users = refreshList(page, searchText);
                class PostRunnable implements Runnable {
                    private List<User> users;
                    private boolean newQuery;

                    @Override
                    public void run() {
                        postResults(users, newQuery);
                        mRefreshSemaphore.release();
                    }

                    public void setUsers(List<User> users) { this.users = users; }
                    public void setNewQuery(boolean newQuery) { this.newQuery = newQuery; }
                };
                PostRunnable postRunnable = new PostRunnable();
                postRunnable.setUsers(users);
                postRunnable.setNewQuery(newQuery);
                mActivity.runOnUiThread(postRunnable);
            }
        };

        mRefreshHandlerInit.release();

        Looper.loop();
    }

    public boolean invokeRefreshList(int iniPage, String searchText, boolean newQuery) {
        if (mRefreshHandler == null) {
            try {
                mRefreshHandlerInit.acquire();
            } catch (InterruptedException ex) {
                return false;
            }
        }

        Message message = mRefreshHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putInt(PAGE_EXTRA, iniPage);
        bundle.putString(SEARCH_TEXT_EXTRA, searchText);
        bundle.putBoolean(NEW_QUERY_EXTRA, newQuery);
        message.setData(bundle);
        mRefreshHandler.sendMessage(message);

        return true;
    }

    private List<User> refreshList(int iniPage, String searchText) {
        final UserServiceApi svc = PotlatchServiceConn.getUserServiceOrShowLogin(mActivity);

        List<User> users = null;
        if (svc != null) {
            InputStream imageInputStream = null;

            try {
                if (iniPage != -1)
                    mIniPage = iniPage;
                if (searchText != null)
                    mSearchText = searchText;

                // Load 4 users pages
                users = new ArrayList<User>();

                mLastPageReached = false;

                if (mOnLoadUsersListener == null)
                    throw new Exception("There is no load users listener");

                int offset = mIniPage * PAGE_SIZE;
                int limit = PAGE_SIZE * 4;
                Collection<User> retrievedUsers =
                        mOnLoadUsersListener.onLoadUsers(mSearchText, offset, limit);
                if (retrievedUsers == null)
                    retrievedUsers = new ArrayList<User>();

                int i = 0;
                for (User retrievedUser : retrievedUsers) {
                    retrievedUser.setIndex(offset + i);
                    users.add(retrievedUser);
                    i++;
                }

                if (retrievedUsers.size() < limit) {
                    mLastPageReached = true;
                }

                /*
                Collection<User> retrievedUsers = null;

                int itPage = mIniPage;
                mLastPageReached = false;
                while (itPage <= mIniPage + 3) {
                    if (itPage >= 0) {
                        if (mOnLoadUsersListener == null)
                            throw new Exception("There is no load users listener");
                        retrievedUsers = mOnLoadUsersListener.onLoadUsers(mSearchText, itPage, PAGE_SIZE);
                        if (retrievedUsers == null)
                            retrievedUsers = new ArrayList<User>();
                        int i = 0;
                        for (User retrievedUser : retrievedUsers) {
                            retrievedUser.setIndex((itPage * PAGE_SIZE) + i);
                            users.add(retrievedUser);
                            i++;
                        }

                        if (retrievedUsers.size() < PAGE_SIZE) {
                            mLastPageReached = true;
                            break;
                        }
                    }
                    itPage++;
                }
                */

                for (User user : users) {

                    String imageId = user.getImageId();
                    if (imageId == null) {
                        //throw new Exception("There is no image id in the user " + user.getLogin());
                        continue;
                    }

                    // If the image isn't already in the file system, download it
                    File imageFile = ImageUtils.getImageFile(imageId);
                    if (!imageFile.exists()) {
                        imageFile = ImageUtils.createImageFile(imageId);

                        Response response =
                                svc.getUserImage(user.getLogin(), UserImageType.NORMAL);
                        imageInputStream = response.getBody().in();
                        FileUtils.copyInputStreamToFile(imageInputStream, imageFile);
                    }

                    user.setImagePath(imageFile.getAbsolutePath());
                }
            } catch (Exception ex) {
                Toast.makeText(
                        mActivity,
                        "Unable to fetch the user list, please log in again:" + ex.toString(),
                        Toast.LENGTH_SHORT).show();

                mActivity.startActivity(new Intent(mActivity, LoginActivity.class));
                return null;
            } finally {
                if (imageInputStream != null)
                    try {
                        imageInputStream.close();
                    } catch (Exception ex) {
                        //error = ex;
                        //return null;
                    }
            }
        }

        return users;
    }

    private void postResults(List<User> users, boolean newQuery) {
        UserListAdapter adapter = (UserListAdapter) mUserListView.getAdapter();
        if (adapter == null) {
            Toast.makeText(
                    mActivity,
                    "There is no adapter to the list",
                    Toast.LENGTH_SHORT).show();
        }

        // Create a new users list using the current list view users and retrieved users
        // List<User> newUsersList = new ArrayList<User>();

        List<User> curUsers = adapter.getUserList();

        // Create map of list view users by login
        /*
        Map<String, User> curUsersByLogin = new HashMap<String, User>();
        for (User curUser : curUsers) {
            curUsersByLogin.put(curUser.getLogin(), curUser);
        }

        // If there is no user with login in list view, add it, else, update it
        for (User user : users) {
            User curUser = curUsersByLogin.get(user.getLogin());
            if (curUser != null) {
                UserUtils.updateUserData(curUser, user);
                newUsersList.add(curUser);
            } else {
                newUsersList.add(user);
            }
        }
        */

        Map<String, User> curUsersByLogin = new HashMap<String, User>();
        Map<String, User> remainsUsersByLogin = new HashMap<String, User>();
        for (User curUser : curUsers) {
            curUsersByLogin.put(curUser.getLogin(), curUser);
            remainsUsersByLogin.put(curUser.getLogin(), curUser);
        }

        // Obtain remaining existing users
        List<User> existingInNewUsers = new ArrayList<User>();
        for (User user : users) {
            User curGift = curUsersByLogin.get(user.getLogin());
            if (curGift != null) {
                existingInNewUsers.add(user);
            }
        }

        // Update existing
        int newPos = 0;
        for (User user : existingInNewUsers) {
            User curUser = curUsersByLogin.get(user.getLogin());

            // Update position
            int curPos = 0;
            int curPosAux = 0;
            for (User curUserAux : adapter.getUserList()) {
                if (user.getLogin().equals(curUserAux.getLogin())) {
                    curPos = curPosAux;
                    break;
                }
                curPosAux++;
            }
            if (curPos != newPos) {
                adapter.remove(curUser);
                adapter.insert(curUser, newPos);
            }

            newPos++;
        }

        adapter.setNotifyOnChange(false);

        // Update data from existing
        for (User user : existingInNewUsers) {
            User curUser = curUsersByLogin.get(user.getLogin());

            UserUtils.updateUserData(curUser, user);

            remainsUsersByLogin.remove(user.getLogin());
        }

        adapter.notifyDataSetChanged();

        // Remove non existing
        for(User curUser : remainsUsersByLogin.values()) {
            adapter.remove(curUser);
        }

        // Insert new
        newPos = 0;
        for (User user : users) {
            User curUser = curUsersByLogin.get(user.getLogin());
            if (curUser == null) {
                adapter.insert(user, newPos);
            }
            newPos++;
        }


        /*
        // Sorts the new users list
        // newUsersList = UserUtils.sortByCreationTimeDesc(newUsersList);
        */

        // Load images, if necessary
        for (User user : users) {
            UserUtils.loadImageBitmap(user);
        }

        /*
        if (newQuery) {
            adapter.setNewUserList(newUsersList);
        } else {
            adapter.updateUserList(newUsersList);
        }
        */
    }

    public void setOnLoadUsersListener(OnLoadUsersListener listener) {
        mOnLoadUsersListener = listener;
    }

    public Semaphore getRefreshSemaphore() {
        return mRefreshSemaphore;
    }

    public int getPageSize() {
        return PAGE_SIZE;
    }

    public int getIniPage() {
        return mIniPage;
    }

    public String getSearchText() {
        return mSearchText;
    }

    public boolean getLastPageReached() {
        return mLastPageReached;
    }
}
