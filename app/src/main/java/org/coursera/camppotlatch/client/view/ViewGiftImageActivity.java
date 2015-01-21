package org.coursera.camppotlatch.client.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.coursera.camppotlatch.R;
import org.coursera.camppotlatch.client.commons.GiftUtils;
import org.coursera.camppotlatch.client.commons.ImageUtils;
import org.coursera.camppotlatch.client.model.Gift;
import org.coursera.camppotlatch.client.model.GiftImageType;
import org.coursera.camppotlatch.client.serviceproxy.GiftServiceApi;
import org.coursera.camppotlatch.client.serviceproxy.PotlatchServiceConn;

import java.io.File;
import java.io.InputStream;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.client.Response;

public class ViewGiftImageActivity extends Activity {
    public static final String GIFT_ID_EXTRA = "giftId";

    @InjectView(R.id.frame)
    protected RelativeLayout mFrame;

    private GiftImageView mGiftImageView;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    private String mGiftId;
    private Gift mGift;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_gift_image);

        ButterKnife.inject(this);

        mGiftId = getIntent().getStringExtra(GIFT_ID_EXTRA);

        mGestureDetector = new GestureDetector(this, mSimpleGestureListener);
        mScaleGestureDetector = new ScaleGestureDetector(this, mSimpleScaleGestureListener);

        mGiftImageView = new GiftImageView(ViewGiftImageActivity.this, null);
        mFrame.addView(mGiftImageView);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_gift_image, menu);
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


    @Override
    protected void onStart() {
        super.onStart();

        refreshView();
    }

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;

        result = mScaleGestureDetector.onTouchEvent(event);
        result = mGestureDetector.onTouchEvent(event) || result;

        return result;
    }

    private final GestureDetector.SimpleOnGestureListener mSimpleGestureListener
            = new GestureDetector.SimpleOnGestureListener()
    {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int curX = mGiftImageView.getCurX() - (int)distanceX;
            int curY = mGiftImageView.getCurY() - (int)distanceY;

            mGiftImageView.setCurPosition(curX, curY);

            return true;
        }
    };

    private final ScaleGestureDetector.SimpleOnScaleGestureListener mSimpleScaleGestureListener
            = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float curScale = mGiftImageView.getCurScale() * scaleFactor;

            mGiftImageView.setCurScale(curScale);

            return true;
        }
    };

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
                                ViewGiftImageActivity.this,
                                "Unable to fetch the gift image: " + error.toString(),
                                Toast.LENGTH_SHORT).show();

                        /*
                        startActivity(new Intent(ViewGiftActivity.this,
                                LoginActivity.class));
                                */

                        return;
                    }

                    Bitmap bitmap = GiftUtils.loadImageBitmap(mGift);
                    mGiftImageView.setBitmap(bitmap);
                }
            };

            refreshTask.execute();
        }
    }
}
