package org.coursera.camppotlatch.client.view;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.coursera.camppotlatch.R;
import org.coursera.camppotlatch.client.commons.AppContext;
import org.coursera.camppotlatch.client.commons.DateUtils;
import org.coursera.camppotlatch.client.commons.ImageUtils;
import org.coursera.camppotlatch.client.model.Gift;
import org.coursera.camppotlatch.client.model.OperationResult;
import org.coursera.camppotlatch.client.model.User;
import org.coursera.camppotlatch.client.serviceproxy.GiftServiceApi;
import org.coursera.camppotlatch.client.serviceproxy.PotlatchServiceConn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.mime.TypedFile;


public class CreateGiftActivity extends Activity {
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_CHOOSE_IMAGE = 2;

    public static final String CAPTION_GIFT_ID_EXTRA = "captionGiftId";
    public static final String GIFT_ID_EXTRA = "giftId";

    @InjectView(R.id.title_text)
    protected EditText mTitle;

    @InjectView(R.id.image_view)
    protected ImageView mImageView;

    @InjectView(R.id.comments_text)
    protected EditText mComments;

    protected String mImageId;
    protected String mImagePath;
    protected boolean hasImage;

    protected String mCaptionGiftId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_gift);

        this.setTitle(R.string.title_activity_new_gift);

        Intent createGiftIntent = getIntent();
        mCaptionGiftId = createGiftIntent.getStringExtra(CAPTION_GIFT_ID_EXTRA);

        ButterKnife.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_gift, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.take_photo_button)
    protected void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(CreateGiftActivity.this,
                    "No camera available", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // Try to create a file to store image
        File imageFile = null;
        try {
            mImageId = ImageUtils.generateImageId();
            imageFile = ImageUtils.createImageFile(mImageId);
            mImagePath = imageFile.getAbsolutePath();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Log.e(CreateGiftActivity.class.getName(),
                    "Exception on creating image file: " + ex.toString());
            Toast.makeText(CreateGiftActivity.this,
                    "Exception on creating image file: " + ex.toString(), Toast.LENGTH_SHORT)
                .show();
            return;
        }

        // Call the camera activity
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(imageFile));
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
    }

    @OnClick(R.id.choose_image_button)
    protected void chooseImage() {
        Intent chooseImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseImageIntent.setType(ImageUtils.JPEG_MIME_TYPE);
        chooseImageIntent.addCategory(Intent.CATEGORY_OPENABLE);

        if (chooseImageIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(CreateGiftActivity.this,
                    "No image chooser available", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        startActivityForResult(chooseImageIntent, REQUEST_CHOOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                //Bundle extras = data.getExtras();
                //Bitmap imageBitmap = (Bitmap) extras.get("data");
                Bitmap imageBitmap = ImageUtils.getBitmap(
                        mImagePath, mImageView.getWidth(), mImageView.getHeight(), false);
                mImageView.setImageBitmap(imageBitmap);
                hasImage = true;
            }
        } else if (requestCode == REQUEST_CHOOSE_IMAGE) {
            if (resultCode == RESULT_OK) {
                //Bundle extras = data.getExtras();
                //Bitmap imageBitmap = (Bitmap) extras.get("data");
                Uri fullPhotoUri = data.getData();
                ContentResolver resolver = getContentResolver();
                ParcelFileDescriptor sourcePhotoFileDesc = null;
                File targetImageFile = null;
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    sourcePhotoFileDesc = resolver.openFileDescriptor(fullPhotoUri, "r");
                    inputStream =
                            new FileInputStream(sourcePhotoFileDesc.getFileDescriptor());

                    mImageId = ImageUtils.generateImageId();
                    targetImageFile = ImageUtils.createImageFile(mImageId);
                    outputStream = new FileOutputStream(targetImageFile);
                    if (IOUtils.copy(inputStream, outputStream) <= 0) {
                        Toast.makeText(CreateGiftActivity.this,
                                "No photo obtained: " + fullPhotoUri.toString(), Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    mImagePath = targetImageFile.getAbsolutePath();
                    Bitmap imageBitmap = ImageUtils.getBitmap(
                            mImagePath, mImageView.getWidth(), mImageView.getHeight(), false);
                    mImageView.setImageBitmap(imageBitmap);
                    hasImage = true;
                } catch (FileNotFoundException ex) {
                    Toast.makeText(CreateGiftActivity.this,
                            "Uri not found: " + fullPhotoUri.toString(), Toast.LENGTH_SHORT)
                            .show();
                    return;
                } catch (IOException ex) {
                    Toast.makeText(CreateGiftActivity.this,
                            "Problems obtaining the photo: " + ex.toString(), Toast.LENGTH_SHORT)
                            .show();
                    return;
                } finally {
                    try {
                        if (sourcePhotoFileDesc != null)
                            sourcePhotoFileDesc.close();
                        if (inputStream != null)
                            inputStream.close();
                        if (outputStream != null)
                            outputStream.close();
                    } catch (IOException ex) {
                        Toast.makeText(CreateGiftActivity.this,
                                "Problems obtaining the photo: "
                                        + ex.toString(), Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                }
            }
        }
    }

    @OnClick(R.id.post_button)
    public void postGift() {
        DateUtils dateUtils = new DateUtils();

        String title = mTitle.getText().toString();
        if (title.equals("")) {
            Toast.makeText(CreateGiftActivity.this, "Insert a title", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        String comments = mComments.getText().toString();
        User user = AppContext.getUser();

        String creatorLogin = user.getLogin();
        String creatorName = user.getName();

        Date createDate = null;
        try {
            createDate = dateUtils.convertToISO8601Date(new Date());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (!hasImage) {
            Toast.makeText(CreateGiftActivity.this, "Take a photo or insert an image", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        Gift gift = new Gift(title, mImageId, comments, creatorLogin, creatorName, createDate,
                mCaptionGiftId);
        gift.setImageContentType(ImageUtils.JPEG_MIME_TYPE);
        gift.setImagePath(mImagePath);

        final GiftServiceApi giftService = PotlatchServiceConn.getGiftServiceOrShowLogin(this);
        AsyncTask<Gift, Void, Gift> task = new AsyncTask<Gift, Void, Gift>() {
            protected Exception error = null;

            @Override
            protected Gift doInBackground(Gift... gifts) {
                Gift gift = gifts[0];
                Gift newGift = giftService.addGift(gifts[0]);

                File imageFile = null;
                try {
                    String imagePath = gift.getImagePath();
                    imageFile = new File(imagePath);
                    if (!imageFile.exists())
                        throw new Exception("There is no " + imagePath + " directory");
                } catch (Exception e) {
                    error = e;
                    return null;
                }

                TypedFile typedImageFile = new TypedFile(gift.getImageContentType(), imageFile);
                OperationResult result = giftService.postGiftImage(newGift.getId(), typedImageFile);

                return newGift;
            }

            @Override
            protected void onPostExecute(Gift gift) {
                super.onPostExecute(gift);

                if (error != null) {
                    Toast.makeText(CreateGiftActivity.this,
                            "Error posting gift: " + error.getMessage(), Toast.LENGTH_SHORT)
                        .show();

                    return;
                }

                //Toast.makeText(CreateGiftActivity.this, "Gift posted", Toast.LENGTH_SHORT);

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

        task.execute(gift);
    }
}
