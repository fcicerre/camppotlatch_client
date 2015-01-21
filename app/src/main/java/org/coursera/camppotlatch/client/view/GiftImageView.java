package org.coursera.camppotlatch.client.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.View;

import org.coursera.camppotlatch.client.commons.ImageUtils;

/**
 * Created by Fabio on 26/11/2014.
 */
public class GiftImageView extends View {
    private Bitmap mOriginalBitmap;

    private int mWidth;
    private int mHeight;

    private int mCurX;
    private int mCurY;
    private float mCurScale;

    private Paint mPaint;
    private DisplayMetrics mDisplayMetrics;
    private int mDisplayWidth;
    private int mDisplayHeight;

    public GiftImageView(Activity activity, Bitmap bitmap) {
        super(activity.getApplicationContext());

        setBitmap(bitmap);

        mCurX = 0;
        mCurY = 0;
        mCurScale = 1.0F;

        mPaint = new Paint();

        mDisplayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        mDisplayWidth = mDisplayMetrics.widthPixels;
        mDisplayHeight = mDisplayMetrics.heightPixels;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mOriginalBitmap != null) {
            int width = (int) (mWidth * mCurScale);
            int height = (int) (mHeight * mCurScale);

            //Bitmap bitmap = Bitmap.createScaledBitmap(mOriginalBitmap, width, height, false);
            canvas.save();

            canvas.scale(mCurScale, mCurScale);
            canvas.translate((float)mCurX / mCurScale, (float)mCurY / mCurScale);
            canvas.drawBitmap(mOriginalBitmap, 0, 0, mPaint);

            canvas.restore();
        }
    }

    public Bitmap getBitmap() {
        return mOriginalBitmap;
    }
    public void setBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            mOriginalBitmap = bitmap;

            mWidth = mOriginalBitmap.getWidth();
            mHeight = mOriginalBitmap.getHeight();

            mCurX = 0;
            mCurY = 0;
            mCurScale = ImageUtils.getScaleFactor(mWidth, mHeight, mDisplayWidth, mDisplayHeight,
                    ImageUtils.FitMode.FIT_ALL);

            postInvalidate();
        }
    }

    public int getCurX() {
        return mCurX;
    }
    public void setCurX(int curX) {
        if (curX > mDisplayWidth - 10)
            curX = mDisplayWidth - 10;
        else if (curX < 10)
            curX = 10;

        mCurX = curX;

        postInvalidate();
    }

    public int getCurY() {
        return mCurY;
    }
    public void setCurY(int curY) {
        if (curY > mDisplayHeight - 10)
            curY = mDisplayHeight - 10;
        else if (curY < 10)
            curY = 10;

        mCurY = curY;

        postInvalidate();
    }

    public void setCurPosition(int curX, int curY) {
        mCurX = curX;
        mCurY = curY;

        postInvalidate();
    }

    public float getCurScale() {
        return mCurScale;
    }
    public void setCurScale(float curScale) {
        if (curScale < 0.2F)
            curScale = 0.2F;
        else if (curScale > 5.0F)
            curScale = 5F;

        mCurScale = curScale;

        postInvalidate();
    }
}
