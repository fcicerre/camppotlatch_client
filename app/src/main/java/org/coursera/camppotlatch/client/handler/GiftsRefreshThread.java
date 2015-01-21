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
import org.coursera.camppotlatch.client.commons.GiftUtils;
import org.coursera.camppotlatch.client.commons.ImageUtils;
import org.coursera.camppotlatch.client.model.Gift;
import org.coursera.camppotlatch.client.model.GiftImageType;
import org.coursera.camppotlatch.client.serviceproxy.GiftServiceApi;
import org.coursera.camppotlatch.client.serviceproxy.PotlatchServiceConn;
import org.coursera.camppotlatch.client.view.GiftListAdapter;
import org.coursera.camppotlatch.client.view.LoginActivity;

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
public class GiftsRefreshThread extends Thread {
    public interface OnLoadGiftsListener {
        public List<Gift> onLoadGifts(String mSearchText, int offset, int limit);
    }

    private static final int PAGE_SIZE = 4;

    private static final String MESSAGE_TYPE_EXTRA = "messageType";

    private static final String PAGE_EXTRA = "page";
    private static final String SEARCH_TEXT_EXTRA = "searchText";
    private static final String NEW_QUERY_EXTRA = "newQuery";
    private static final String UNCHECK_ALL_EXTRA = "uncheckAll";

    private static final String GIFT_ID_EXTRA = "giftId";

    private static final String REFRESH_LIST_MESSAGE_TYPE = "invokeRefreshList";
    private static final String REFRESH_GIFT_MESSAGE_TYPE = "invokeRefreshGift";

    private Activity mActivity;
    private ListView mGiftListView;

    private Handler mRefreshHandler = null;
    private Semaphore mRefreshHandlerInit = new Semaphore(0, true);
    private Semaphore mRefreshSemaphore = new Semaphore(1, false);

    private int mIniPage = 0;
    private String mSearchText = null;
    private boolean mLastPageReached = false;

    private OnLoadGiftsListener mOnLoadGiftsListener;

    public GiftsRefreshThread(Activity activity,
                              ListView giftListView,
                              OnLoadGiftsListener onLoadGiftsListener) {
        mActivity = activity;
        mGiftListView = giftListView;
        mOnLoadGiftsListener = onLoadGiftsListener;
    }

    @Override
    public void run() {
        Looper.prepare();

        mRefreshHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Bundle bundle = msg.getData();
                String messageType = bundle.getString(MESSAGE_TYPE_EXTRA);
                if (messageType.equals(REFRESH_LIST_MESSAGE_TYPE)) {
                    int page = bundle.getInt(PAGE_EXTRA);
                    String searchText = bundle.getString(SEARCH_TEXT_EXTRA);
                    boolean newQuery = bundle.getBoolean(NEW_QUERY_EXTRA);
                    boolean unCheckAll = bundle.getBoolean(UNCHECK_ALL_EXTRA);

                    List<Gift> gifts = refreshList(page, searchText);
                    class PostRunnable implements Runnable {
                        private List<Gift> gifts;
                        private boolean newQuery;
                        private boolean unCheckAll;

                        @Override
                        public void run() {
                            postGiftListRefresh(gifts, newQuery, unCheckAll);
                            mRefreshSemaphore.release();
                        }

                        public void setGifts(List<Gift> gifts) {
                            this.gifts = gifts;
                        }
                        public void setNewQuery(boolean newQuery) {
                            this.newQuery = newQuery;
                        }
                        public void setUnCheckAll(boolean unCheckAll) {
                            this.unCheckAll = unCheckAll;
                        }
                    }
                    ;
                    PostRunnable postRunnable = new PostRunnable();
                    postRunnable.setGifts(gifts);
                    postRunnable.setNewQuery(newQuery);
                    mActivity.runOnUiThread(postRunnable);
                } else if (messageType.equals(REFRESH_GIFT_MESSAGE_TYPE)) {
                    String giftId = bundle.getString(GIFT_ID_EXTRA);

                    final Gift gift = refreshGift(giftId);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            postGiftRefresh(gift);
                        }
                    });
                }
            }
        };

        mRefreshHandlerInit.release();

        Looper.loop();
    }

    public boolean invokeRefreshList(int iniPage, String searchText,
                                     boolean newQuery, boolean unCheckAll) {
        if (mRefreshHandler == null) {
            try {
                mRefreshHandlerInit.acquire();
            } catch (InterruptedException ex) {
                return false;
            }
        }

        Message message = mRefreshHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString(MESSAGE_TYPE_EXTRA, REFRESH_LIST_MESSAGE_TYPE);
        bundle.putInt(PAGE_EXTRA, iniPage);
        bundle.putString(SEARCH_TEXT_EXTRA, searchText);
        bundle.putBoolean(NEW_QUERY_EXTRA, newQuery);
        bundle.putBoolean(UNCHECK_ALL_EXTRA, unCheckAll);
        message.setData(bundle);
        mRefreshHandler.sendMessage(message);

        return true;
    }

    public boolean invokeRefreshGift(String giftId) {
        if (mRefreshHandler == null) {
            try {
                mRefreshHandlerInit.acquire();
            } catch (InterruptedException ex) {
                return false;
            }
        }

        Message message = mRefreshHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString(MESSAGE_TYPE_EXTRA, REFRESH_GIFT_MESSAGE_TYPE);
        bundle.putString(GIFT_ID_EXTRA, giftId);
        message.setData(bundle);
        mRefreshHandler.sendMessage(message);

        return true;
    }

    private List<Gift> refreshList(int iniPage, String searchText) {
        final GiftServiceApi svc = PotlatchServiceConn.getGiftServiceOrShowLogin(mActivity);

        List<Gift> gifts = null;
        if (svc != null) {
            try {
                if (iniPage != -1)
                    mIniPage = iniPage;
                if (searchText != null)
                    mSearchText = searchText;

                // Load until limit elements from offset
                gifts = new ArrayList<Gift>();

                //int itPage = mIniPage;
                mLastPageReached = false;

                if (mOnLoadGiftsListener == null)
                    throw new Exception("There is no load gifts listener");

                int offset = mIniPage * PAGE_SIZE;
                int limit = PAGE_SIZE * 4;
                Collection<Gift> retrievedGifts = mOnLoadGiftsListener.onLoadGifts(mSearchText, offset, limit);
                if (retrievedGifts == null)
                    retrievedGifts = new ArrayList<Gift>();

                int i = 0;
                for (Gift retrievedGift : retrievedGifts) {
                    retrievedGift.setIndex(offset + i);
                    gifts.add(retrievedGift);
                    i++;
                }

                if (retrievedGifts.size() < limit) {
                    mLastPageReached = true;
                }

                /*
                while (itPage <= mIniPage + 3) {
                    if (itPage >= 0) {
                        //retrievedGifts = svc.findAll(minDateStr, maxDateStr, mSearchText, itPage, PAGE_SIZE);
                        if (mOnLoadGiftsListener == null)
                            throw new Exception("There is no load gifts listener");
                        retrievedGifts = mOnLoadGiftsListener.onLoadGifts(mSearchText, itPage, PAGE_SIZE);
                        if (retrievedGifts == null)
                            retrievedGifts = new ArrayList<Gift>();
                        int i = 0;
                        for (Gift retrievedGift : retrievedGifts) {
                            retrievedGift.setIndex((itPage * PAGE_SIZE) + i);
                            gifts.add(retrievedGift);
                            i++;
                        }

                        if (retrievedGifts.size() < PAGE_SIZE) {
                            mLastPageReached = true;
                            break;
                        }
                    }
                    itPage++;
                }
                */

                for (Gift gift : gifts) {
                    loadGift(gift, svc);
                    /*
                    String imageId = gift.getImageId();
                    if (imageId == null) {
                        //throw new Exception("There is no image id in the gift " + gift.getId());
                        continue;
                    }

                    // If the image isn't already in the file system, download it
                    File imageFile = ImageUtils.getThumbnailImageFile(imageId);
                    if (!imageFile.exists()) {
                        imageFile = ImageUtils.createThumbnailImageFile(imageId);

                        Response response =
                                svc.getGiftImage(gift.getId(), GiftImageType.THUMBNAIL);
                        imageInputStream = response.getBody().in();
                        FileUtils.copyInputStreamToFile(imageInputStream, imageFile);
                    }

                    gift.setThumbnailImagePath(imageFile.getAbsolutePath());
                    */

                    /*
                    Integer isLiked = svc.isGiftLiked(gift.getId());
                    gift.setLikeFlag(isLiked);

                    Integer isMarkedInappropriate = svc.isGiftMarkedInappropriate(gift.getId());
                    gift.setInappropriateFlag(isMarkedInappropriate);
                    */
                }
            } catch (Exception ex) {
                Toast.makeText(
                        mActivity,
                        "Unable to fetch the gift list, please log in again:" + ex.toString(),
                        Toast.LENGTH_SHORT).show();

                mActivity.startActivity(new Intent(mActivity, LoginActivity.class));
                return null;
            }
        }

        return gifts;
    }

    private Gift refreshGift(String giftId) {
        final GiftServiceApi svc = PotlatchServiceConn.getGiftServiceOrShowLogin(mActivity);

        Gift gift = null;
        if (svc != null) {
            try {
                gift = svc.findById(giftId);
                loadGift(gift, svc);

            } catch (Exception ex) {
                Toast.makeText(
                        mActivity,
                        "Unable to fetch the gift list, please log in again:" + ex.toString(),
                        Toast.LENGTH_SHORT).show();

                mActivity.startActivity(new Intent(mActivity, LoginActivity.class));
                return null;
            }
        }

        return gift;
    }

    private void loadGift(Gift gift, GiftServiceApi svc) {
        InputStream imageInputStream = null;

        String imageId = gift.getImageId();
        if (imageId == null) {
            //throw new Exception("There is no image id in the gift " + gift.getId());
            return;
        }

        try {
            // If the image isn't already in the file system, download it
            File imageFile = ImageUtils.getThumbnailImageFile(imageId);
            if (!imageFile.exists()) {
                imageFile = ImageUtils.createThumbnailImageFile(imageId);

                Response response =
                        svc.getGiftImage(gift.getId(), GiftImageType.THUMBNAIL);
                imageInputStream = response.getBody().in();
                FileUtils.copyInputStreamToFile(imageInputStream, imageFile);
            }

            gift.setThumbnailImagePath(imageFile.getAbsolutePath());

                    /*
                    Integer isLiked = svc.isGiftLiked(gift.getId());
                    gift.setLikeFlag(isLiked);

                    Integer isMarkedInappropriate = svc.isGiftMarkedInappropriate(gift.getId());
                    gift.setInappropriateFlag(isMarkedInappropriate);
                    */
        } catch (Exception ex) {
            Toast.makeText(
                    mActivity,
                    "Unable to load gift, please log in again:" + ex.toString(),
                    Toast.LENGTH_SHORT).show();

            mActivity.startActivity(new Intent(mActivity, LoginActivity.class));
        } finally {
            if (imageInputStream != null)
                try {
                    imageInputStream.close();
                } catch (Exception ex) {
                }
        }
    }

    private void postGiftListRefresh(List<Gift> gifts, boolean newQuery, boolean unCheckAll) {
        GiftListAdapter adapter = (GiftListAdapter) mGiftListView.getAdapter();
        if (adapter == null) {
            Toast.makeText(
                    mActivity,
                    "There is no adapter to the list",
                    Toast.LENGTH_SHORT).show();
        }

        // Create a new gifts list using the current list view gifts and retrieved gifts
        // List<Gift> newGiftsList = new ArrayList<Gift>();

        List<Gift> curGifts = adapter.getGiftList();

        // Create map of list view gifts by id
        /*
        Map<String, Gift> curGiftsById = new HashMap<String, Gift>();
        for (Gift curGift : curGifts) {
            curGiftsById.put(curGift.getId(), curGift);
        }
        */

        // If there is no gift with id in list view, add it, else, update it
        /*
        for (Gift gift : gifts) {
            Gift curGift = curGiftsById.get(gift.getId());
            if (curGift != null) {
                GiftUtils.updateGiftData(curGift, gift);
                newGiftsList.add(curGift);
            } else {
                newGiftsList.add(gift);
            }
        }
        */

        /*
        List<Gift> curGiftsCopy = Lists.newArrayList(curGifts);

        Map<String, Integer> curGiftsPosById = new HashMap<String, Integer>();
        int pos = 0;
        for (Gift curGift : curGiftsCopy) {
            curGiftsPosById.put(curGift.getId(), pos);
            pos++;
        }
        */

        Map<String, Gift> curGiftsById = new HashMap<String, Gift>();
        Map<String, Gift> remainsGiftsById = new HashMap<String, Gift>();
        for (Gift curGift : curGifts) {
            curGiftsById.put(curGift.getId(), curGift);
            remainsGiftsById.put(curGift.getId(), curGift);
        }

        // Obtain remaining existing gifts
        List<Gift> existingInNewGifts = new ArrayList<Gift>();
        for (Gift gift : gifts) {
            Gift curGift = curGiftsById.get(gift.getId());
            if (curGift != null) {
                existingInNewGifts.add(gift);
            }
        }

        // Update position from existing
        int newPos = 0;
        for (Gift gift : existingInNewGifts) {
            Gift curGift = curGiftsById.get(gift.getId());

            // Update position
            int curPos = 0;
            int curPosAux = 0;
            for (Gift curGiftAux : adapter.getGiftList()) {
                if (gift.getId().equals(curGiftAux.getId())) {
                    curPos = curPosAux;
                    break;
                }
                curPosAux++;
            }
            if (curPos != newPos) {
                adapter.remove(curGift);
                adapter.insert(curGift, newPos);
            }

            newPos++;
        }

        adapter.setNotifyOnChange(false);

        // Update data from existing
        for (Gift gift : existingInNewGifts) {
            Gift curGift = curGiftsById.get(gift.getId());

            GiftUtils.updateGiftData(curGift, gift);
            if (unCheckAll) {
                gift.setSelected(false);
            }

            remainsGiftsById.remove(gift.getId());
        }

        adapter.notifyDataSetChanged();

        // Remove non existing
        for(Gift curGift : remainsGiftsById.values()) {
            adapter.remove(curGift);
        }

        // Insert new
        newPos = 0;
        for (Gift gift : gifts) {
            Gift curGift = curGiftsById.get(gift.getId());
            if (curGift == null) {
                adapter.insert(gift, newPos);
            }
            newPos++;
        }

        // Sorts the new gifts list
        // newGiftsList = GiftUtils.sortByCreationTimeDesc(newGiftsList);

        // Add gifts to the list and discard inappropriate ones
        // if the user settings ask to do so
        /*
        User currentUser = AppContext.getUser();
        List<Gift> finalGiftsList = new ArrayList<Gift>();
        for (Gift gift : newGiftsList) {
            if (currentUser.getDisableInappropriate()
                    && gift.getInappropriateCount() > 0
                    && !gift.getCreatorLogin().equals(currentUser.getLogin()))
                continue;
            GiftUtils.loadThumbnailImageBitmap(gift);
            finalGiftsList.add(gift);
        }
        */

        /*
        // Remove inappropriate gifts, if necessary
        User currentUser = AppContext.getUser();
        for (Gift gift : gifts) {
            if (currentUser.getDisableInappropriate()
                    && gift.getInappropriateCount() > 0
                    && !gift.getCreatorLogin().equals(currentUser.getLogin())) {

                adapter.remove(gift);
            } else {
                GiftUtils.loadThumbnailImageBitmap(gift);
            }
        }
        */

        // Load images, if necessary
        for (Gift gift : gifts) {
            GiftUtils.loadThumbnailImageBitmap(gift);
        }

        /*
        if (newQuery) {
            adapter.setNewGiftList(newGiftsList);
        } else {
            adapter.updateGiftList(newGiftsList);
        } */
    }

    private void postGiftRefresh(Gift gift) {
        GiftListAdapter adapter = (GiftListAdapter) mGiftListView.getAdapter();
        if (adapter == null) {
            Toast.makeText(
                    mActivity,
                    "There is no adapter to the list",
                    Toast.LENGTH_SHORT).show();
        }

        adapter.updateGift(gift);
    }

    public void setOnLoadGiftsListener(OnLoadGiftsListener listener) {
        mOnLoadGiftsListener = listener;
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
