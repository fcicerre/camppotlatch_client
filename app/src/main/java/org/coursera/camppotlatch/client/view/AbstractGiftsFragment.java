package org.coursera.camppotlatch.client.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import org.coursera.camppotlatch.R;
import org.coursera.camppotlatch.client.commons.AppContext;
import org.coursera.camppotlatch.client.handler.GiftsRefreshThread;
import org.coursera.camppotlatch.client.model.Gift;
import org.coursera.camppotlatch.client.model.OperationResult;
import org.coursera.camppotlatch.client.androidservice.DataRefreshService;
import org.coursera.camppotlatch.client.androidservice.IDataRefreshService;
import org.coursera.camppotlatch.client.androidservice.IDataRefreshServiceCallback;
import org.coursera.camppotlatch.client.serviceproxy.GiftServiceApi;
import org.coursera.camppotlatch.client.serviceproxy.PotlatchServiceConn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractGiftsFragment extends Fragment {
    private static final int CREATE_GIFT_REQUEST_CODE = 1;
    private static final int EDIT_GIFT_REQUEST_CODE = 2;

    private Activity mActivity;

    private String mUserLogin;
    private String mCaptionGiftId;

    private IDataRefreshService mRefreshService;
    private boolean mIsRefreshServiceBound = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mRefreshService = IDataRefreshService.Stub.asInterface(iBinder);

            try {
                mRefreshService.setUpdatePeriod(AppContext.getUser().getGiftsReloadPeriod());
                mClientId = mRefreshService.addUpdatePeriodClient(mRefreshServiceClient);
                mIsRefreshServiceBound = true;
            } catch (RemoteException ex) {
                if (mActivity != null) {
                    Toast.makeText(
                            mActivity,
                            "Unable to set the update service",
                            Toast.LENGTH_SHORT).show();
                }
                mRefreshService = null;
                mIsRefreshServiceBound = false;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mRefreshService = null;
            mIsRefreshServiceBound = false;
        }
    };

    private int mClientId;
    private IDataRefreshServiceCallback.Stub mRefreshServiceClient = new IDataRefreshServiceCallback.Stub() {
        @Override
        public void notifyUpdate() {
            if (mActivity != null && mVisible) {
                Handler handler = new Handler(mActivity.getMainLooper());

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mRefreshThread.getRefreshSemaphore().tryAcquire()) {
                            mRefreshThread.invokeRefreshList(-1, null, false, false);
                        }
                    }
                });
            }
        }
    };

    private GiftsRefreshThread mRefreshThread;

    protected boolean mVisible;

    protected ListView mGiftListView;

    public AbstractGiftsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_abstract_gifts, container, false);

        mGiftListView = (ListView)view.findViewById(R.id.gift_list_view);
        initializeListView();

        configureChoiceMode();

        mRefreshThread = new GiftsRefreshThread(mActivity,
                mGiftListView,
                new GiftsRefreshThread.OnLoadGiftsListener() {
                    @Override
                    public List<Gift> onLoadGifts(String mSearchText, int offset, int limit) {
                        return loadGifts(mSearchText, offset, limit);
                    }
                });
        mRefreshThread.start();

        try {
            mRefreshThread.getRefreshSemaphore().acquire();
            mRefreshThread.invokeRefreshList(0, "", true, true);

            mGiftListView.setOnScrollListener(giftListViewScrollListener);
        } catch (InterruptedException ex) {}
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mVisible = true;

        Intent serviceIntent = new Intent(mActivity, DataRefreshService.class);
        mActivity.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();

        mVisible = false;

        if (mIsRefreshServiceBound) {
            try {
                mRefreshService.removeUpdatePeriodClient(mClientId);
            } catch (Exception ex) {
                Toast.makeText(
                        mActivity,
                        "Unable to remove the update service client",
                        Toast.LENGTH_SHORT).show();
            }

            mActivity.unbindService(mServiceConnection);
            mIsRefreshServiceBound = false;
        }
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
        if (mUserLogin != null && !AppContext.getUser().getLogin().equals(mUserLogin)) {
            inflater.inflate(R.menu.abstract_gifts_without_create, menu);
        } else {
            inflater.inflate(R.menu.abstract_gifts, menu);
        }

        // Search widget configuration
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mRefreshThread.invokeRefreshList(0, s, true, true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s == null || s.equals("")) {
                    mRefreshThread.invokeRefreshList(0, s, true, true);
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
            case R.id.action_post:
                createGift();
                return true;
            case R.id.action_search:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setUserLogin(String userLogin) {
        mUserLogin = userLogin;
    }
    public String getUserLogin() {
        return mUserLogin;
    }

    public void setCaptionGiftId(String captionGiftId) {
        mCaptionGiftId = captionGiftId;
    }
    public String getCaptionGiftId() {
        return mCaptionGiftId;
    }

    private void initializeListView() {
        View.OnClickListener imageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ImageView imageView = (ImageView)view;
                Gift gift = (Gift) view.getTag();
                viewGift(gift);
            }
        };

        View.OnLongClickListener imageLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Gift gift = (Gift) view.getTag();
                return showGiftItemMenu(view, gift);
            }
        };

        View.OnClickListener creatorNameClickListener = null;
        if (mUserLogin == null) {
            creatorNameClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Gift gift = (Gift) view.getTag();
                    viewUserGifts(gift);
                }
            };
        }

        View.OnClickListener likesClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Gift gift = (Gift) view.getTag();
                likeGift(gift);
            }
        };

        View.OnClickListener reportClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Gift gift = (Gift) view.getTag();
                reportGift(gift);
            }
        };

        View.OnClickListener relatedGiftsClickListener = null;
        if (mCaptionGiftId == null) {
            relatedGiftsClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Gift gift = (Gift) view.getTag();
                    viewRelatedGifts(gift);
                }
            };
        }

        mGiftListView.setAdapter(new GiftListAdapter(
                mActivity,
                R.layout.gift_list_item, new ArrayList<Gift>(),
                mActivity, (ViewGroup)mGiftListView.getParent(),
                imageClickListener, imageLongClickListener, creatorNameClickListener,
                likesClickListener, reportClickListener,
                relatedGiftsClickListener));
    }

    protected abstract List<Gift> loadGifts(String mSearchText, int offset, int limit);

    private void configureChoiceMode() {
        mGiftListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mGiftListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                GiftListAdapter giftListAdapter = (GiftListAdapter) mGiftListView.getAdapter();
                int mItensChecked = giftListAdapter.getGiftsSelectedCount();

                actionMode.setTitle(mItensChecked + " itens selected");
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater menuInflater = mActivity.getMenuInflater();
                menuInflater.inflate(R.menu.gift_choice_item_menu, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.remove_gift:
                        removeSelectedGifts();
                        actionMode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });
    }

    private AbsListView.OnScrollListener giftListViewScrollListener =
            new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                }

                @Override
                public void onScroll(AbsListView listView,
                                     int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                    GiftListAdapter adapter = (GiftListAdapter) mGiftListView.getAdapter();
                    if (adapter.getGiftList().size() == 0)
                        return;

                    Gift firstGift = adapter.getItem(firstVisibleItem);
                    if (firstGift == null)
                        return;

                    int pageVisible = (firstGift.getIndex() / mRefreshThread.getPageSize());

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
                                mRefreshThread.invokeRefreshList(nextIniPage, null, false, false);
                            }
                        }
                    } else if (pageVisible >= curIniPage + 2) {
                        if (!lastPageReached) {
                            if (pageVisible <= 1)
                                nextIniPage = 0;
                            else
                                nextIniPage = pageVisible - 1;
                            if (mRefreshThread.getRefreshSemaphore().tryAcquire()) {
                                mRefreshThread.invokeRefreshList(nextIniPage, null, false, false);
                            }
                        }
                    }
                }
            };

    private boolean showGiftItemMenu(View view, final Gift gift) {
        if (!gift.getCreatorLogin().equals(AppContext.getUser().getLogin())) {
            return false;
        }

        PopupMenu popupMenu = new PopupMenu(mActivity, view);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.action_edit_gift:
                        editGift(gift);
                        return true;
                    case R.id.action_remove_gift:
                        secureRemoveGift(gift);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.inflate(R.menu.gift_item_menu);
        popupMenu.show();

        return true;
    }

    private void createGift() {
        Intent createGiftIntent = new Intent(mActivity, CreateGiftActivity.class);
        createGiftIntent.putExtra(CreateGiftActivity.CAPTION_GIFT_ID_EXTRA, mCaptionGiftId);
        startActivityForResult(createGiftIntent, CREATE_GIFT_REQUEST_CODE);
    }

    private void editGift(final Gift gift) {
        Intent editGiftIntent = new Intent(mActivity, EditGiftActivity.class);
        editGiftIntent.putExtra(EditGiftActivity.GIFT_ID_EXTRA, gift.getId());
        startActivityForResult(editGiftIntent, EDIT_GIFT_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_GIFT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                mRefreshThread.invokeRefreshList(-1, null, false, true);
            }
        } else if (requestCode == EDIT_GIFT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String giftId = data.getStringExtra(CreateGiftActivity.GIFT_ID_EXTRA);
                if (!giftId.equals("")) {
                    mRefreshThread.invokeRefreshGift(giftId);
                } else {
                    mRefreshThread.invokeRefreshList(-1, null, false, false);
                }
            }
        }
    }

    private void secureRemoveGift(final Gift gift) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity);

        dialogBuilder.setMessage("Remove this gift?");

        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                removeGift(gift);
            }
        });

        dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        dialogBuilder.create().show();
    }

    private void removeGift(final Gift gift) {
        final GiftServiceApi giftService = PotlatchServiceConn.getGiftServiceOrShowLogin(mActivity);
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            protected Exception error = null;

            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    OperationResult result = giftService.removeGift(gift.getId());
                } catch (Exception e) {
                    error = e;
                    return null;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);

                if (error != null) {
                    Toast.makeText(mActivity,
                            "Error removing gift: " + error.getMessage(), Toast.LENGTH_SHORT)
                            .show();

                    return;
                }

                Toast.makeText(mActivity, "Gift removed", Toast.LENGTH_SHORT);

                mRefreshThread.invokeRefreshList(-1, null, false, false);
            }
        };

        task.execute();
    }

    private void viewGift(Gift gift) {
        //Intent giftViewIntent = new Intent(mActivity, ViewGiftActivity.class);
        Intent giftViewIntent = new Intent(mActivity, ViewGiftImageActivity.class);

        giftViewIntent.putExtra(ViewGiftActivity.GIFT_ID_EXTRA, gift.getId());
        startActivity(giftViewIntent);
    }

    private void likeGift(Gift gift) {
        final GiftServiceApi svc = PotlatchServiceConn.getGiftServiceOrShowLogin(mActivity);

        if (svc != null) {
            AsyncTask<Gift, Void, Gift>
                    likeTask = new AsyncTask<Gift, Void, Gift>() {
                protected Exception error = null;

                @Override
                protected Gift doInBackground(Gift... gifts) {
                    Gift gift = gifts[0];
                    int likeFlag = gift.getLikeFlag();

                    try {
                        if (likeFlag == 0)
                            svc.likeGift(gift.getId());
                        else
                            svc.unlikeGift(gift.getId());
                    } catch (Exception ex) {
                        error = ex;
                        return null;
                    } finally {
                    }

                    return gift;
                }

                @Override
                protected void onPostExecute(Gift gift) {
                    if (error != null) {
                        Toast.makeText(
                                mActivity,
                                "Unable to like/unlike the gifts",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //mRefreshThread.invokeRefreshList(-1, null, false);
                    mRefreshThread.invokeRefreshGift(gift.getId());
                }
            };

            likeTask.execute(gift);
        }
    }

    private void reportGift(Gift gift) {
        final GiftServiceApi svc = PotlatchServiceConn.getGiftServiceOrShowLogin(mActivity);

        if (svc != null) {
            AsyncTask<Gift, Void, Gift>
                    markInappropriateTask = new AsyncTask<Gift, Void, Gift>() {
                protected Exception error = null;

                @Override
                protected Gift doInBackground(Gift... gifts) {
                    Gift gift = gifts[0];
                    int inappropriateFlag = gift.getInappropriateFlag();

                    try {
                        if (inappropriateFlag == 0)
                            svc.markInappropriateGift(gift.getId());
                        else
                            svc.unmarkInappropriateGift(gift.getId());
                    } catch (Exception ex) {
                        error = ex;
                        return null;
                    } finally {
                    }

                    return gift;
                }

                @Override
                protected void onPostExecute(Gift gift) {
                    if (error != null) {
                        Toast.makeText(
                                mActivity,
                                "Unable to mark/unmark the gifts as inappropriate",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //mRefreshThread.invokeRefreshList(-1, null, false);
                    mRefreshThread.invokeRefreshGift(gift.getId());
                }
            };

            markInappropriateTask.execute(gift);
        }
    }

    private void viewUserGifts(Gift gift) {
        Intent userGiftsActivityIntent = new Intent(mActivity, UserGiftsActivity.class);
        userGiftsActivityIntent.putExtra(UserGiftsActivity.USER_LOGIN_EXTRA, gift.getCreatorLogin());
        startActivity(userGiftsActivityIntent);
    }

    private void viewRelatedGifts(Gift gift) {
        Intent relatedGiftsActivityIntent = new Intent(mActivity, RelatedGiftsActivity.class);
        relatedGiftsActivityIntent.putExtra(RelatedGiftsActivity.CAPTION_GIFT_ID_EXTRA, gift.getId());
        startActivity(relatedGiftsActivityIntent);
    }

    private void removeSelectedGifts() {
        final GiftServiceApi svc = PotlatchServiceConn.getGiftServiceOrShowLogin(mActivity);

        GiftListAdapter giftListAdapter = (GiftListAdapter)mGiftListView.getAdapter();
        Collection<Gift> selectedGifts = giftListAdapter.getSelectedGifts();

        if (svc != null) {
            AsyncTask<Collection<Gift>, Void, Void>
                    removeTask = new AsyncTask<Collection<Gift>, Void, Void>() {
                protected Exception error = null;

                @Override
                protected Void doInBackground(Collection<Gift>... giftsCollections) {
                    Collection<Gift> gifts = giftsCollections[0];

                    try {
                        for (Gift gift : gifts) {
                            svc.removeGift(gift.getId());
                        }
                    } catch (Exception ex) {
                        error = ex;
                        return null;
                    } finally {
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void voids) {
                    if (error != null) {
                        Toast.makeText(
                                mActivity,
                                "Unable to remove the gifts",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mRefreshThread.invokeRefreshList(-1, null, false, true);
                }
            };

            removeTask.execute(selectedGifts);
        }
    }
}
