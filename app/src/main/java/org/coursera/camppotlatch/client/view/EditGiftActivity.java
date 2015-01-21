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
import android.widget.EditText;
import android.widget.ImageView;
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

public class EditGiftActivity extends Activity {
    public static final String GIFT_ID_EXTRA = "giftId";

    @InjectView(R.id.title_text)
    protected EditText mTitle;

    @InjectView(R.id.image_view)
    protected ImageView mImageView;

    @InjectView(R.id.comments_text)
    protected EditText mComments;

    protected String mGiftId;
    protected Gift mGift;

    private DisplayMetrics mDisplayMetrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_gift);

        this.setTitle("Edit Gift");

        ButterKnife.inject(this);

        Intent createGiftIntent = getIntent();
        mGiftId = createGiftIntent.getStringExtra(GIFT_ID_EXTRA);

        mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

        refreshView();
    }

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
                                EditGiftActivity.this,
                                "Unable to fetch the gift image: " + error.toString(),
                                Toast.LENGTH_SHORT).show();

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

    @OnClick(R.id.save_button)
    public void postGift() {
        String title = mTitle.getText().toString();
        if (title.equals("")) {
            Toast.makeText(EditGiftActivity.this, "Insert a title", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        String comments = mComments.getText().toString();

        mGift.setTitle(title);
        mGift.setComments(comments);

        final GiftServiceApi giftService = PotlatchServiceConn.getGiftServiceOrShowLogin(this);
        AsyncTask<Gift, Void, Gift> task = new AsyncTask<Gift, Void, Gift>() {
            protected Exception error = null;

            @Override
            protected Gift doInBackground(Gift... gifts) {
                Gift gift = gifts[0];

                try {
                    OperationResult operationResult = giftService.updateGift(gift.getId(), gift);
                    if (operationResult.getResult() == OperationResult.OperationResultState.FAILED)
                        throw new Exception("Error updating the gift");
                } catch (Exception e) {
                    error = e;
                    return null;
                }

                return gift;
            }

            @Override
            protected void onPostExecute(Gift gift) {
                super.onPostExecute(gift);

                if (error != null) {
                    Toast.makeText(EditGiftActivity.this,
                            "Error saving gift: " + error.getMessage(), Toast.LENGTH_SHORT)
                            .show();

                    return;
                }

                //Toast.makeText(EditGiftActivity.this, "Gift saved", Toast.LENGTH_SHORT);
                Intent data = new Intent();
                data.putExtra(GIFT_ID_EXTRA, gift.getId());
                if (getParent() == null) {
                    setResult(Activity.RESULT_OK, data);
                } else {
                    getParent().setResult(Activity.RESULT_OK, data);
                }
                finish();
            }
        };

        task.execute(mGift);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_gift, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_remove_gift:
                secureRemoveGift();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

        dialogBuilder.create();
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
                    Toast.makeText(EditGiftActivity.this,
                            "Error removing gift: " + error.getMessage(), Toast.LENGTH_SHORT)
                            .show();

                    return;
                }

                //Toast.makeText(EditGiftActivity.this, "Gift removed", Toast.LENGTH_SHORT);
                Intent data = new Intent();
                data.putExtra(GIFT_ID_EXTRA, "");
                if (getParent() == null) {
                    setResult(Activity.RESULT_OK, data);
                } else {
                    getParent().setResult(Activity.RESULT_OK, data);
                }
                finish();
            }
        };

        task.execute();
    }
}
