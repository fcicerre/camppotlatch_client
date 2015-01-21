package org.coursera.camppotlatch.client.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.coursera.camppotlatch.R;
import org.coursera.camppotlatch.client.commons.DateUtils;
import org.coursera.camppotlatch.client.commons.UserUtils;
import org.coursera.camppotlatch.client.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Fabio on 22/11/2014.
 */
public class UserListAdapter extends ArrayAdapter<User> {
    private Context mContext;
    private List<User> mUserList;

    private View.OnClickListener mUserNameClickListener;

    private Map<String, User> mSelectedUsersMap;
    private int mUsersSelectedCount;

    public UserListAdapter(Context context, int resource, List<User> userList,
                           View.OnClickListener userNameClickListener) {
        super(context, resource, userList);

        mContext = context;
        mUserList = userList;

        mUserNameClickListener = userNameClickListener;

        mSelectedUsersMap = new HashMap<String, User>();
        mUsersSelectedCount = 0;
    }

    private class ViewHolder {
        View userItemView;
        int position;

        User user;

        ImageView imageView;

        TextView userNameTextView;
        TextView userCityTextView;
        TextView userCountryTextView;
        TextView userCreationTimeTextView;
        TextView userPostedGiftsCountTextView;
        TextView userReceivedLikesCountTextView;
    }

    @Override
    public View getView(int position, View userItemView, ViewGroup parent) {
        ViewHolder holder = null;
        Log.v("User item view", String.valueOf(position));

        if (userItemView == null) {
            LayoutInflater li = (LayoutInflater)mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            userItemView = li.inflate(R.layout.user_list_item, parent, false);

            holder = new ViewHolder();

            holder.userItemView = userItemView;

            holder.imageView = (ImageView)userItemView.findViewById(R.id.image_view);

            holder.userNameTextView = (TextView)userItemView.findViewById(R.id.user_name_text_view);
            holder.userCityTextView = (TextView)userItemView.findViewById(R.id.user_city_text_view);
            holder.userCountryTextView = (TextView)userItemView.findViewById(R.id.user_country_text_view);
            holder.userCreationTimeTextView = (TextView)userItemView.findViewById(R.id.user_creation_time_text_view);
            holder.userPostedGiftsCountTextView = (TextView)userItemView.findViewById(R.id.user_gifts_count_text_view);
            holder.userReceivedLikesCountTextView = (TextView)userItemView.findViewById(R.id.user_received_likes_count_text_view);

            userItemView.setTag(holder);
        }
        else {
            holder = (ViewHolder)userItemView.getTag();
        }

        DateUtils dateUtils = new DateUtils();

        User user = mUserList.get(position);

        holder.position = position;
        holder.user = user;

        Bitmap bitmap = UserUtils.loadImageBitmap(user);
        holder.imageView.setScaleType(ImageView.ScaleType.FIT_START);
        holder.imageView.setImageBitmap(bitmap);
        //holder.imageView.setTag(user);

        holder.userNameTextView.setText(user.getName());
        holder.userNameTextView.setTag(user);
        if (mUserNameClickListener != null)
            holder.userNameTextView.setOnClickListener(mUserNameClickListener);

        holder.userCityTextView.setText(user.getCity());
        holder.userCountryTextView.setText(user.getCountry());

        holder.userCreationTimeTextView.setText(dateUtils.printYYYYMMDDFormat(user.getCreateTime()));

        holder.userPostedGiftsCountTextView.setText(user.getPostedGiftsCount().toString());

        holder.userReceivedLikesCountTextView.setText(user.getLikesCount().toString());

        return userItemView;
    }

    public void setNewUserList(List<User> userList) {
        clear();
        addAll(userList);
    }

    public void updateUserList(List<User> userList) {
        mUserList = userList;
        notifyDataSetChanged();
    }

    public List<User> getUserList() {
        return mUserList;
    }
}
