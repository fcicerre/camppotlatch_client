package org.coursera.camppotlatch.client.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.coursera.camppotlatch.R;
import org.coursera.camppotlatch.client.commons.GiftUtils;
import org.coursera.camppotlatch.client.commons.ImageUtils;
import org.coursera.camppotlatch.client.model.Gift;
import org.coursera.camppotlatch.client.model.GiftImageType;
import org.coursera.camppotlatch.client.model.OperationResult;
import org.coursera.camppotlatch.client.serviceproxy.GiftServiceApi;
import org.coursera.camppotlatch.client.serviceproxy.PotlatchServiceConn;

import java.io.File;
import java.io.InputStream;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.client.Response;

public class ViewGiftActivity extends Activity {

    public static final String GIFT_ID_EXTRA = "giftId";

    @InjectView(R.id.title_text_view)
    protected TextView mTitle;

    @InjectView(R.id.image_view)
    protected ImageView mImageView;

    @InjectView(R.id.comments_text_view)
    protected TextView mComments;

    private String mGiftId;
    private Gift mGift;

    private DisplayMetrics mDisplayMetrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_gift);

        ButterKnife.inject(this);

        mGiftId = getIntent().getStringExtra(GIFT_ID_EXTRA);

        mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
    }

    @Override
    protected void onStart() {
        super.onStart();

        refreshView();
    }

    /*
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
    */

    private void refreshView() {
        final GiftServiceApi svc = PotlatchServiceConn.getGiftServiceOrShowLogin(this);

        if (svc != null) {
            AsyncTask<Void, Void, Void>
                    refreshTask = new AsyncTask<Void, Void, Void>() {
                protected Exception error = null;

                @Override
                protected Void doInBackground(Void... voids) {
                    InputStream imageInputStream = null;

                    try {
                        // Get the gift
                        mGift = svc.findById(mGiftId);
                        if (mGift == null)
                            throw new Exception("There is no gift with id " + mGiftId);

                        String imageId = mGift.getImageId();
                        if (imageId == null) {
                            //throw new Exception("There is no image id in the gift " + mGiftId);
                            return null;
                        }

                        // If the image isn't already in the file system, download it
                        File imageFile = ImageUtils.getImageFile(imageId);
                        if (!imageFile.exists()) {
                            imageFile = ImageUtils.createImageFile(imageId);

                            Response response =
                                    svc.getGiftImage(mGift.getId(), GiftImageType.NORMAL);
                            imageInputStream = response.getBody().in();
                            FileUtils.copyInputStreamToFile(imageInputStream, imageFile);
                        }

                        mGift.setImagePath(imageFile.getAbsolutePath());
                        //GiftUtils.loadThumbnailImageBitmap(gift);
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
                                ViewGiftActivity.this,
                                "Unable to fetch the gift image: " + error.toString(),
                                Toast.LENGTH_SHORT).show();

                        /*
                        startActivity(new Intent(ViewGiftActivity.this,
                                LoginActivity.class));
                                */

                        return;
                    }

                    mTitle.setText(mGift.getTitle());

                    Bitmap bitmap = GiftUtils.loadImageBitmap(mGift,
                            mDisplayMetrics.widthPixels,
                            (int)((float)mDisplayMetrics.heightPixels * 0.8F), false);
                    mImageView.setImageBitmap(bitmap);

                    if (mGift.getComments() != null) {
                        mComments.setText(mGift.getComments());
                    }
                }
            };

            refreshTask.execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_gift, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit_gift:
                editGift();
                return true;
            case R.id.action_remove_gift:
                secureRemoveGift();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void editGift() {
        Intent editGiftIntent = new Intent(this, EditGiftActivity.class);
        editGiftIntent.putExtra(EditGiftActivity.GIFT_ID_EXTRA, mGiftId);
        startActivity(editGiftIntent);
    }

    private void secureRemoveGift() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setMessage("Remove this gift?");

        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                removeGift();
            }
        });

        dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        dialogBuilder.create().show();
    }

    private void removeGift() {
        final GiftServiceApi giftService = PotlatchServiceConn.getGiftServiceOrShowLogin(this);
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            protected Exception error = null;

            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    OperationResult result = giftService.removeGift(mGiftId);
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
                    Toast.makeText(ViewGiftActivity.this,
                            "Error removing gift: " + error.getMessage(), Toast.LENGTH_SHORT)
                            .show();

                    return;
                }

                Toast.makeText(ViewGiftActivity.this, "Gift removed", Toast.LENGTH_SHORT);
                finish();
            }
        };

        task.execute();
    }

    @OnClick(R.id.image_view)
    protected void viewImage() {
        Intent viewImageIntent = new Intent(this, ViewGiftImageActivity.class);
        viewImageIntent.putExtra(ViewGiftImageActivity.GIFT_ID_EXTRA, mGiftId);
        startActivity(viewImageIntent);
    }
}
