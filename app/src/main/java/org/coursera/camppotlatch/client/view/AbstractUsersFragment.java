package org.coursera.camppotlatch.client.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SearchView;

import org.coursera.camppotlatch.R;
import org.coursera.camppotlatch.client.handler.UsersRefreshThread;
import org.coursera.camppotlatch.client.model.User;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractUsersFragment extends Fragment {

    private Activity mActivity;

    private UsersRefreshThread mRefreshThread;

    protected ListView mUserListView;

    public AbstractUsersFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_abstract_users, container, false);

        mUserListView = (ListView)view.findViewById(R.id.user_list_view);
        initializeListView();

        mRefreshThread = new UsersRefreshThread(mActivity,
                mUserListView,
                new UsersRefreshThread.OnLoadUsersListener() {
                    @Override
                    public List<User> onLoadUsers(String mSearchText, int offset, int limit) {
                        return loadUsers(mSearchText, offset, limit);
                    }
                });
        mRefreshThread.start();

        try {
            mRefreshThread.getRefreshSemaphore().acquire();
            mRefreshThread.invokeRefreshList(0, "", true);

            mUserListView.setOnScrollListener(userListViewScrollListener);
        } catch (InterruptedException ex) {}

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mActivity = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.abstract_users, menu);

        // Search widget configuration
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mRefreshThread.invokeRefreshList(0, s, true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s == null || s.equals("")) {
                    mRefreshThread.invokeRefreshList(0, s, true);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.action_search:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void initializeListView() {
        View.OnClickListener userNameClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = (User) view.getTag();
                viewUserGifts(user);
            }
        };

        mUserListView.setAdapter(new UserListAdapter(
                mActivity,
                R.layout.user_list_item, new ArrayList<User>(),
                userNameClickListener));
    }

    protected abstract List<User> loadUsers(String mSearchText, int offset, int limit);

    private AbsListView.OnScrollListener userListViewScrollListener =
            new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                }

                @Override
                public void onScroll(AbsListView listView,
                                     int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                    UserListAdapter adapter = (UserListAdapter) mUserListView.getAdapter();
                    if (adapter.getUserList().size() == 0)
                        return;

                    User firstUser = adapter.getItem(firstVisibleItem);
                    if (firstUser == null)
                        return;

                    int pageVisible = (firstUser.getIndex() / mRefreshThread.getPageSize());

                    int curIniPage = mRefreshThread.getIniPage();
                    boolean lastPageReached = mRefreshThread.getLastPageReached();
                    int nextIniPage = 0;
                    if (pageVisible <= curIniPage) {
                        if (curIniPage > 0) {
                            if (pageVisible <= 1)
                                nextIniPage = 0;
                            else
                                nextIniPage = pageVisible - 1;
                            if (mRefreshThread.getRefreshSemaphore().tryAcquire()) {
                                mRefreshThread.invokeRefreshList(nextIniPage, null, false);
                            }
                        }
                    } else if (pageVisible >= curIniPage + 2) {
                        if (!lastPageReached) {
                            if (pageVisible <= 1)
                                nextIniPage = 0;
                            else
                                nextIniPage = pageVisible - 1;
                            if (mRefreshThread.getRefreshSemaphore().tryAcquire()) {
                                mRefreshThread.invokeRefreshList(nextIniPage, null, false);
                            }
                        }
                    }
                }
            };

    private void viewUserGifts(User user) {
        Intent userGiftsActivityIntent = new Intent(mActivity, UserGiftsActivity.class);
        userGiftsActivityIntent.putExtra(UserGiftsActivity.USER_LOGIN_EXTRA, user.getLogin());
        startActivity(userGiftsActivityIntent);
    }
}
