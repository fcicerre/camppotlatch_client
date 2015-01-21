package org.coursera.camppotlatch.client.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.coursera.camppotlatch.R;
import org.coursera.camppotlatch.client.commons.ImageUtils;
import org.coursera.camppotlatch.client.commons.UserUtils;
import org.coursera.camppotlatch.client.model.OperationResult;
import org.coursera.camppotlatch.client.model.User;
import org.coursera.camppotlatch.client.model.UserImageType;
import org.coursera.camppotlatch.client.serviceproxy.PotlatchServiceConn;
import org.coursera.camppotlatch.client.serviceproxy.UserServiceApi;

import java.io.File;
import java.io.InputStream;

import retrofit.client.Response;

public class ViewUserAccountFragment extends Fragment {
    public static final String USER_LOGIN_EXTRA = "userLogin";

    private Activity mActivity;

    protected ImageView mImageView;

    protected TextView mName;

    protected TextView mEmail;

    protected TextView mCity;

    protected TextView mCountry;

    protected TextView mPostedGiftsCount;

    protected TextView mReceivedLikesCount;

    protected TextView mHideInappropriate;

    protected TextView mUpdatePeriod;

    private String mUserLogin;
    private User mUser;


    public ViewUserAccountFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_user_account, container, false);

        mImageView = (ImageView)view.findViewById(R.id.image_view);

        mName = (TextView)view.findViewById(R.id.user_name_text_view);
        mEmail = (TextView)view.findViewById(R.id.user_email_text_view);
        mCity = (TextView)view.findViewById(R.id.user_city_text_view);
        mCountry = (TextView)view.findViewById(R.id.user_country_text_view);

        mPostedGiftsCount = (TextView)view.findViewById(R.id.user_posted_gifts_count_text_view);
        mReceivedLikesCount = (TextView)view.findViewById(R.id.user_received_likes_count_text_view);

        mHideInappropriate = (TextView)view.findViewById(R.id.user_hide_inappropriate_text_view);
        mUpdatePeriod = (TextView)view.findViewById(R.id.user_gifts_update_period_text_view);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = activity;
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshView();
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
        inflater.inflate(R.menu.view_user_account, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_edit_user:
                editUser();
                return true;
            case R.id.action_change_password_user:
                changePassword();
                return true;
            case R.id.action_remove_user:
                secureRemoveUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setArguments(Bundle bundle) {
        super.setArguments(bundle);

        mUserLogin = bundle.getString(USER_LOGIN_EXTRA);
    }

    private void refreshView() {
        final UserServiceApi svc = PotlatchServiceConn.getUserServiceOrShowLogin(mActivity);

        if (svc != null) {
            AsyncTask<Void, Void, Void>
                    refreshTask = new AsyncTask<Void, Void, Void>() {
                protected Exception error = null;

                @Override
                protected Void doInBackground(Void... voids) {
                    InputStream imageInputStream = null;

                    try {
                        // Get the user
                        mUser = svc.findByLogin(mUserLogin);
                        if (mUser == null)
                            throw new Exception("There is no user with login " + mUser);

                        String imageId = mUser.getImageId();
                        if (imageId == null) {
                            //throw new Exception("There is no image id in the user " + mUserLogin);
                            return null;
                        }

                        // If the image isn't already in the file system, download it
                        File imageFile = ImageUtils.getImageFile(imageId);
                        if (!imageFile.exists()) {
                            imageFile = ImageUtils.createImageFile(imageId);

                            Response response =
                                    svc.getUserImage(mUser.getLogin(), UserImageType.NORMAL);
                            imageInputStream = response.getBody().in();
                            FileUtils.copyInputStreamToFile(imageInputStream, imageFile);
                        }

                        mUser.setImagePath(imageFile.getAbsolutePath());
                    } catch (Exception ex) {
                        error = ex;
                        return null;
                    } finally {
                        if (imageInputStream != null)
                            try {
                                imageInputStream.close();
                            } catch (Exception ex) {
                                error = ex;
                                return null;
                            }
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void voids) {
                    super.onPostExecute(null);

                    if (error != null) {
                        Toast.makeText(
                                mActivity,
                                "Unable to fetch the user image: " + error.toString(),
                                Toast.LENGTH_SHORT).show();

                        return;
                    }

                    Bitmap bitmap = UserUtils.loadImageBitmap(mUser);
                    mImageView.setImageBitmap(bitmap);

                    mName.setText(mUser.getName());
                    mEmail.setText(mUser.getEmail());
                    mCity.setText(mUser.getCity());
                    mCountry.setText(mUser.getCountry());

                    mPostedGiftsCount.setText(mUser.getPostedGiftsCount().toString());
                    mReceivedLikesCount.setText(mUser.getLikesCount().toString());

                    if (mUser.getDisableInappropriate())
                        mHideInappropriate.setText("hide");
                    else
                        mHideInappropriate.setText("show");

                    mUpdatePeriod.setText(mUser.getGiftsReloadPeriod().toString() + " min");
                }
            };

            refreshTask.execute();
        }
    }

    private void editUser() {
        Intent editUserIntent = new Intent(mActivity, EditUserAccountActivity.class);
        editUserIntent.putExtra(EditUserAccountActivity.USER_LOGIN_EXTRA, mUserLogin);
        startActivity(editUserIntent);
    }

    private void changePassword() {
        Intent changePasswordIntent = new Intent(mActivity, ChangePasswordUserAccountActivity.class);
        changePasswordIntent.putExtra(ChangePasswordUserAccountActivity.USER_LOGIN_EXTRA,
                mUserLogin);
        startActivity(changePasswordIntent);
    }

    private void secureRemoveUser() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity);

        dialogBuilder.setMessage("Remove this user?");

        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                removeUser();
            }
        });

        dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        dialogBuilder.create().show();
    }

    private void removeUser() {
        final UserServiceApi userService = PotlatchServiceConn.getUserServiceOrShowLogin(mActivity);
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            protected Exception error = null;

            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    OperationResult result = userService.removeUser(mUserLogin);
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
                            "Error removing user: " + error.getMessage(), Toast.LENGTH_SHORT)
                            .show();

                    return;
                }

                Toast.makeText(mActivity, "User removed", Toast.LENGTH_SHORT);
                Intent loginIntent = new Intent(mActivity, LoginActivity.class);
                startActivity(loginIntent);
            }
        };

        task.execute();
    }
}
