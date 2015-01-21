package org.coursera.camppotlatch.client.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.coursera.camppotlatch.R;
import org.coursera.camppotlatch.client.commons.AppContext;
import org.coursera.camppotlatch.client.commons.DateUtils;
import org.coursera.camppotlatch.client.commons.GiftUtils;
import org.coursera.camppotlatch.client.model.Gift;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Fabio on 09/11/2014.
 */
public class GiftListAdapter extends ArrayAdapter<Gift> {
    private Context mContext;
    private List<Gift> mGiftList;

    private Activity mActivity;
    private ViewGroup mParent;

    private View.OnClickListener mImageClickListener;
    private View.OnLongClickListener mImageLongClickListener;
    private View.OnClickListener mCreatorNameClickListener;
    private View.OnClickListener mLikeClickListener;
    private View.OnClickListener mReportClickListener;
    private View.OnClickListener mRelatedGiftsClickListener;

    //private Map<String, Gift> mSelectedGiftsMap;
    //private int mGiftsSelectedCount;

    public GiftListAdapter(Context context, int resource, List<Gift> giftList,
                           Activity activity, ViewGroup parent,
                           View.OnClickListener imageClickListener,
                           View.OnLongClickListener imageLongClickListener,
                           View.OnClickListener creatorNameClickListener,
                           View.OnClickListener likeClickListener,
                           View.OnClickListener reportClickListener,
                           View.OnClickListener relatedGiftsClickListener) {
        super(context, resource, giftList);

        mContext = context;
        mGiftList = giftList;

        mActivity = activity;
        mParent = parent;

        mImageClickListener = imageClickListener;
        mImageLongClickListener = imageLongClickListener;
        mCreatorNameClickListener = creatorNameClickListener;
        mLikeClickListener = likeClickListener;
        mReportClickListener = reportClickListener;
        mRelatedGiftsClickListener = relatedGiftsClickListener;

        //mSelectedGiftsMap = new HashMap<String, Gift>();
        //mGiftsSelectedCount = 0;
    }

    private class ViewHolder {
        View giftItemView;
        int position;

        Gift gift;

        TextView titleTextView;
        FrameLayout titleBorder;
        ImageView imageView;
        CheckBox giftSelectionCheckbox;
        TextView creatorTextView;
        TextView createDateTextView;
        ImageView likeButton;
        TextView likesCountTextView;
        TextView relatedCountTextView;
        TextView retatedTextView;
        ImageView reportButton;
        TextView commentsTextView;
    }

    private class ItemSelectionHandler implements CompoundButton.OnCheckedChangeListener {
        protected int mPosition;
        protected View mItemView;
        protected ListView mParent;
        protected Gift mGift;

        public ItemSelectionHandler(int position, View itemView, ListView parent, Gift gift) {
            mPosition = position;
            mItemView = itemView;
            mParent = parent;
            mGift = gift;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            Gift gift = mGiftList.get(mPosition);

            /*
            if (b) {
                if (!mSelectedGiftsMap.containsKey(gift.getId())) {
                    mSelectedGiftsMap.put(gift.getId(), gift);
                    //mGiftsSelectedCount++;
                }
            } else {
                if (mSelectedGiftsMap.containsKey(gift.getId())) {
                    mSelectedGiftsMap.remove(gift.getId());
                    //mGiftsSelectedCount--;
                }
            }
            */

            gift.setSelected(b);
            mParent.setItemChecked(mPosition, b);
        }
    }

    @Override
    public View getView(int position, View giftItemView, ViewGroup parent) {
        ViewHolder holder = null;
        Log.v("Gift item view", String.valueOf(position));

        if (giftItemView == null) {
            LayoutInflater li = (LayoutInflater)mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            giftItemView = li.inflate(R.layout.gift_list_item, parent, false);

            holder = new ViewHolder();

            holder.giftItemView = giftItemView;

            holder.titleTextView = (TextView)giftItemView.findViewById(R.id.title_text_view);
            holder.titleBorder = (FrameLayout)giftItemView.findViewById(R.id.title_border);

            holder.imageView = (ImageView)giftItemView.findViewById(R.id.image_view);
            if (mImageClickListener != null)
                holder.imageView.setOnClickListener(mImageClickListener);
            if (mImageLongClickListener != null)
                holder.imageView.setOnLongClickListener(mImageLongClickListener);

            holder.giftSelectionCheckbox = (CheckBox) giftItemView.findViewById(R.id.gift_selection_checkbox);

            holder.creatorTextView = (TextView)giftItemView.findViewById(R.id.creator_text_view);
            if (mCreatorNameClickListener != null)
                holder.creatorTextView.setOnClickListener(mCreatorNameClickListener);

            holder.createDateTextView = (TextView)giftItemView.findViewById(R.id.create_date_text_view);

            holder.likeButton = (ImageView)giftItemView.findViewById(R.id.like_button);
            if (mLikeClickListener != null)
                holder.likeButton.setOnClickListener(mLikeClickListener);

            holder.likesCountTextView = (TextView)giftItemView.findViewById(R.id.likes_count_text_view);

            holder.reportButton = (ImageView)giftItemView.findViewById(R.id.report_button);
            if (mReportClickListener != null)
                holder.reportButton.setOnClickListener(mReportClickListener);

            holder.relatedCountTextView = (TextView)giftItemView.findViewById(R.id.related_count_text_view);

            holder.retatedTextView = (TextView)giftItemView.findViewById(R.id.related_text_view);
            if (mRelatedGiftsClickListener != null)
                holder.retatedTextView.setOnClickListener(mRelatedGiftsClickListener);

            holder.commentsTextView = (TextView)giftItemView.findViewById(R.id.comments_text_view);

            giftItemView.setTag(holder);
        }
        else {
            holder = (ViewHolder)giftItemView.getTag();
        }

        DateUtils dateUtils = new DateUtils();

        Gift gift = mGiftList.get(position);

        holder.position = position;
        holder.gift = gift;

        Bitmap bitmap = GiftUtils.loadThumbnailImageBitmap(gift);
        holder.imageView.setScaleType(ImageView.ScaleType.FIT_START);
        holder.imageView.setImageBitmap(bitmap);
        holder.imageView.setTag(gift);

        //int bitmapWidth = bitmap.getWidth();

        /*
        if (mSelectedGiftsMap.containsKey(gift.getId())) {
            holder.giftSelectionCheckbox.setChecked(true);
        } else {
            holder.giftSelectionCheckbox.setChecked(false);
        }*/

        holder.giftSelectionCheckbox.setOnCheckedChangeListener(
                new ItemSelectionHandler(position, giftItemView, (ListView) parent, gift));
        holder.giftSelectionCheckbox.setChecked(gift.getSelected());
        if (gift.getCreatorLogin().equals(AppContext.getUser().getLogin())) {
            holder.giftSelectionCheckbox.setVisibility(View.VISIBLE);
        } else {
            holder.giftSelectionCheckbox.setVisibility(View.INVISIBLE);
        }

        holder.titleTextView.setText(gift.getTitle());

        holder.creatorTextView.setText(gift.getCreatorName());
        holder.creatorTextView.setTag(gift);

        holder.createDateTextView.setText(dateUtils.printYYYYMMDDFormat(gift.getCreateTime()));

        Drawable likeResource = null;
        if (gift.getLikeFlag() == 1) {
            likeResource =
                    mContext.getResources().getDrawable(R.drawable.ic_action_good_marked);
        } else {
            likeResource =
                    mContext.getResources().getDrawable(R.drawable.ic_action_good);
        }
        holder.likeButton.setImageDrawable(likeResource);
        holder.likeButton.setTag(gift);

        holder.likesCountTextView.setText(gift.getLikesCount().toString());

        Drawable reportResource = null;
        if (gift.getInappropriateCount() > 0) {
            reportResource =
                    mContext.getResources().getDrawable(R.drawable.ic_action_report_flag_marked);
        } else {
            reportResource =
                    mContext.getResources().getDrawable(R.drawable.ic_action_report_flag);
        }
        holder.reportButton.setImageDrawable(reportResource);
        holder.reportButton.setTag(gift);

        if (gift.getCaptionGiftId() == null) {
            holder.relatedCountTextView.setText(gift.getRelatedCount().toString());
            if (gift.getRelatedCount() == 1) {
                holder.retatedTextView.setText(" gift");
            } else {
                holder.retatedTextView.setText(" gifts");
            }
        } else {
            holder.relatedCountTextView.setVisibility(View.INVISIBLE);
            holder.retatedTextView.setVisibility(View.INVISIBLE);
        }
        holder.retatedTextView.setTag(gift);

        holder.commentsTextView.setText(gift.getComments());

        return giftItemView;
    }

    public Collection<Gift> getSelectedGifts() {
        List<Gift> selectedList = new ArrayList<Gift>();
        for (Gift gift : mGiftList) {
            if (gift.getSelected())
                selectedList.add(gift);
        }

        return selectedList;
    }

    public int getGiftsSelectedCount() {
        int count = 0;
        for (Gift gift : mGiftList) {
            if (gift.getSelected())
                count++;
        }

        return count;
    }

    public void setNewGiftList(List<Gift> giftList) {
        clear();
        addAll(giftList);
    }

    public void updateGiftList(List<Gift> giftList) {
        mGiftList = giftList;
        notifyDataSetChanged();
    }

    public List<Gift> getGiftList() {
        return mGiftList;
    }

    public void updateGift(Gift gift) {
        Gift selectedGift = null;
        for(Gift curGift : mGiftList) {
            if (curGift.getId().equals(gift.getId())) {
                selectedGift = curGift;
            }
        }

        if (selectedGift != null) {
            GiftUtils.updateGiftData(selectedGift, gift);
            notifyDataSetChanged();
        }
    }

    /*
    public void unCheckAll() {
        int count = getCount();
        for (int pos = 0; pos < count; pos++) {
            View view = getView(pos, null, mParent);
            ViewHolder holder = (ViewHolder)view.getTag();
            holder.giftSelectionCheckbox.setChecked(false);
        }
    }
    */
}
