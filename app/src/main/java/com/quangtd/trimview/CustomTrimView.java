package com.quangtd.trimview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;

import static com.quangtd.trimview.CustomTrimView.MIN_DURATION;

/**
 * QuangTD on 10/4/2017.
 */

public class CustomTrimView extends View {

    interface OnTouchControllerListener {
        void onMoveHead();

        void onMoveTail();
    }

    private boolean mIsScrollable;
    private float firstTouchX;
    private int backupRectX;

    public static final int MAX_DURATION = 180000; //ms
    public static final int MIN_DURATION = 5000;   //ms

    private int DEFAULT_HEIGHT = 150; //dp

    private boolean mIsPrepared;
    private String mVideoPath;
    private VideoView mVideoView;
    private float mSDuration;
    private float mMSDuration;

    private OnTouchControllerListener mListener;
    private Paint mPaint;
    private Paint mPaintImage;
    private Paint mPaintText;
    private Context mContext;

    private Rect mRectPreview;
    private Rect mRectTimeView;
    private int mWidth;
    private int mHeight;
    private int mPadding;

    private Control mControlHead;
    private Control mControlTail;
    private float mStartTime;
    private float mEndTime;
    private float mCurrentPosition;
    private Handler mHandler;

    private List<Bitmap> mThumbnails;
    private int mNumberOfPreview;

    public CustomTrimView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomTrimView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        mContext = context;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaintImage = new Paint(Paint.FILTER_BITMAP_FLAG);
        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText.setTextSize(Utils.convertDpToPixel(mContext, 15));
        mPaintText.setColor(Color.argb(255, 153, 153, 153));

        mPadding = Utils.convertDpToPixel(context, 10);

        mWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        mHeight = Utils.convertDpToPixel(getContext(), DEFAULT_HEIGHT);

        mRectPreview = new Rect(mPadding, 0, mWidth - mPadding, mHeight / 2);
        mRectTimeView = new Rect(mPadding, mHeight / 2, mWidth - mPadding, mHeight);

        mControlHead = new Control(mContext, Control.TYPE.HEAD, mSDuration, mRectPreview);
        mControlTail = new Control(mContext, Control.TYPE.TAIL, mSDuration, mRectPreview);
        mControlHead.setOther(mControlTail);
        mControlTail.setOther(mControlHead);

        mThumbnails = new ArrayList<>();
        mHandler = new Handler();
    }


    public void setData(VideoView videoView, String videoPath) {
        setVideoPath(videoPath);
        setVideoView(videoView);
        mIsPrepared = true;
    }

    private void setVideoPath(String videoPath) {
        this.mVideoPath = videoPath;
    }

    private void setVideoView(VideoView videoView) {
        if (this.mVideoView != null) return;
        this.mVideoView = videoView;
        this.mSDuration = videoView.getDuration() / 1000;
        this.mMSDuration = videoView.getDuration();
        mControlHead.mMSDuration = mMSDuration;
        mControlTail.mMSDuration = mMSDuration;

        mIsScrollable = (mMSDuration > MAX_DURATION);
        mStartTime = 0;
        if (mIsScrollable) {
            int newWidth = (int) (mMSDuration / MAX_DURATION * mRectPreview.width());
            mRectPreview.set(mRectPreview.left, mRectPreview.top, mRectPreview.left + newWidth, mRectPreview.bottom);
            mRectTimeView.set(mRectTimeView.left, mRectTimeView.top, mRectTimeView.left + newWidth, mRectTimeView.bottom);
            mEndTime = MAX_DURATION;
        } else {
            mEndTime = mMSDuration;
        }

        mNumberOfPreview = getNUmberOfPreview();
        mThumbnails = Utils.getThumbnails(mVideoPath, mMSDuration, mNumberOfPreview, mRectPreview.width() / mNumberOfPreview, mHeight / 2);
        mHandler.removeCallbacksAndMessages(null);
        mHandler.post(mRunnable);
        invalidate();
    }

    private Runnable mRunnable = new Runnable() {
        int DELAY = 25;

        public void run() {
            mHandler.postDelayed(this, DELAY);
            mCurrentPosition = mVideoView.getCurrentPosition();
            if (mCurrentPosition + DELAY > mEndTime) {
                mVideoView.seekTo((int) mStartTime);
                mVideoView.pause();
                invalidate();
            }
            if (mVideoView.isPlaying()) {
                invalidate();
            }
        }
    };

    public void setListener(OnTouchControllerListener listener) {
        this.mListener = listener;
    }

    public float[] getValues() {
        return new float[]{mStartTime, mEndTime};
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTrimView(canvas);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        mHeight = Utils.convertDpToPixel(getContext(), DEFAULT_HEIGHT);
        setMeasuredDimension(mWidth, mHeight);
    }

    private void drawTrimView(Canvas canvas) {
        drawPreviewView(canvas);
        drawTimeView(canvas);


    }

    private void drawPreviewView(Canvas canvas) {
        //draw images
        mPaint.setColor(Color.BLACK);
        canvas.drawRect(mRectPreview, mPaint);
        if (mIsPrepared) {
            for (int i = 0; i < mNumberOfPreview; i++) {
                Bitmap bitmap = mThumbnails.get(i);
                canvas.drawBitmap(bitmap, mRectPreview.left + i * mRectPreview.width() / mNumberOfPreview, mRectPreview.top, mPaintImage);
            }
        }

        //draw cover color
        mPaint.setColor(Color.BLACK);
        mPaint.setAlpha(200);
        canvas.drawRect(0, 0, mControlHead.x, mHeight / 2, mPaint);
        canvas.drawRect(mControlTail.x, 0, mWidth, mHeight / 2, mPaint);

        //draw current position
        mPaint.setColor(Color.WHITE);
        canvas.drawRect(mRectPreview.left + mCurrentPosition / mMSDuration * mRectPreview.width(), 0, mRectPreview.left + mCurrentPosition / mMSDuration * mRectPreview.width() + 5, mHeight / 2, mPaint);

        //draw timeline
        mPaint.setAlpha(255);
        mPaint.setColor(Color.argb(255, 56, 151, 240));
        canvas.drawRect(mControlHead.x, 0, mControlTail.x, 10, mPaint);

        //draw control
        mControlHead.draw(canvas);
        mControlTail.draw(canvas);
    }

    private void drawTimeView(Canvas canvas) {
        // one second ~~ stepDimen pixels.
        float stepDimen = (mRectTimeView.width() * 1.0f / mSDuration);

        int widthLine10 = Utils.convertDpToPixel(mContext, 2);
        int widthLine2 = Utils.convertDpToPixel(mContext, 1);
        int heightLine10 = Utils.convertDpToPixel(mContext, (int) (0.6f * DEFAULT_HEIGHT / 2));
        int heightLine2 = Utils.convertDpToPixel(mContext, (int) (0.3f * DEFAULT_HEIGHT / 2));

        mPaint.setColor(Color.argb(255, 153, 153, 153));

        float[] stepDraw = getStep();
        for (float i = 0; i <= mSDuration; i += 0.5f) {
            int startX = (int) (i * stepDimen + mRectTimeView.left);
            if (i % stepDraw[0] == 0 || i == mSDuration) {
                // draw big
                canvas.drawRect(startX, mHeight - heightLine10, startX + widthLine10, mHeight, mPaint);
                int widthText = (int) mPaintText.measureText(Utils.convertDurationToString((int) i));
                canvas.drawText(Utils.convertDurationToString((int) i), startX - widthText / 2, mHeight - heightLine10 - 10, mPaintText);
            } else if (i % stepDraw[1] == 0) {
                //draw small
                canvas.drawRect(startX, mHeight - heightLine2, startX + widthLine2, mHeight, mPaint);
            }
        }
    }

    private boolean isTouchInside(float x, float y, Control control) {
        return (x < (control.x + control.r) && (x > control.x - control.r) &&
                (y < mRectPreview.bottom) && (y > mRectPreview.top));

    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mIsScrollable) {
                    firstTouchX = event.getX();
                    backupRectX = mRectTimeView.left;
                }

                if (isTouchInside(event.getX(), event.getY(), mControlHead)) {
                    mControlHead.isTouch = true;
                    invalidate();
                }

                if (isTouchInside(event.getX(), event.getY(), mControlTail)) {
                    mControlTail.isTouch = true;
                    invalidate();
                }

                break;
            case MotionEvent.ACTION_UP:
                if (mControlHead.isTouch) {
                    mVideoView.start();
                    mControlHead.isTouch = false;
                }
                if (mControlTail.isTouch) {
                    mControlTail.isTouch = false;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mControlHead.isTouch) {
                    mControlHead.setX((int) event.getX());
                    if (mVideoView != null) {
                        mVideoView.pause();
                        mVideoView.seekTo((int) (mControlHead.getCurrent()));
                    }
                    if (mListener != null) {
                        mListener.onMoveHead();
                    }
                    mStartTime = mControlHead.getCurrent();
                    invalidate();

                } else if (mControlTail.isTouch) {
                    mControlTail.setX((int) event.getX());
                    if (mVideoView != null) {
                        mVideoView.pause();
                        mVideoView.seekTo((int) (mControlTail.getCurrent()));
                    }
                    if (mListener != null) {
                        mListener.onMoveTail();
                    }
                    mEndTime = mControlTail.getCurrent();
                    invalidate();

                } else if (mIsScrollable) {

                    int dx = (int) (event.getX() - firstTouchX);
                    if ((backupRectX + mRectTimeView.width() + dx) <= (mWidth - mPadding)) {
                        break;
                    }
                    if ((backupRectX + dx) >= (mPadding)) {
                        break;
                    }
                    mRectTimeView.set(backupRectX + dx, mRectTimeView.top, backupRectX + mRectTimeView.width() + dx, mRectTimeView.bottom);
                    mRectPreview.set(backupRectX + dx, mRectPreview.top, backupRectX + mRectPreview.width() + dx, mRectPreview.bottom);
                    mStartTime = mControlHead.getCurrent();
                    mEndTime = mControlTail.getCurrent();
                    mVideoView.seekTo((int) mStartTime);
                    invalidate();
                }
                break;
        }
        return true;

    }

    private float[] getStep() {
        if (mSDuration < 20) return new float[]{1, 0.5f};
        if (mSDuration < 60) return new float[]{5, 1};
        if (mSDuration < 90) return new float[]{10, 1};
        if (mSDuration < 180) return new float[]{20, 4};
        else return new float[]{30, 5};
    }

    private int getNUmberOfPreview() {
        if (this.mSDuration < 10) return 5;
        else return 10;
    }


}

class Control {

    enum TYPE {
        HEAD,
        TAIL
    }

    float mMSDuration;
    private TYPE mType;
    int x, y, r;
    private Paint mPaint;
    private int mWidthParent;
    private int mHeightParent;
    private int mWidthLine;
    private Control other;
    boolean isTouch;
    private Rect bound;

    Control(Context context, TYPE type, float duration, Rect bound) {
        this.mType = type;
        this.mMSDuration = duration;//miliseconds
        this.bound = bound;
        this.mWidthParent = bound.width();
        this.mHeightParent = bound.height();
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        r = Utils.convertDpToPixel(context, 10);
        if (type == TYPE.HEAD) x = bound.left;
        if (type == TYPE.TAIL) x = bound.right;
        y = mHeightParent / 2;
        mWidthLine = Utils.convertDpToPixel(context, 2);
    }

    void draw(Canvas canvas) {
        if (isTouch) {
            mPaint.setColor(Color.WHITE);
        } else {
            mPaint.setColor(Color.LTGRAY);
        }
        canvas.drawRect(x, 0, x + mWidthLine, mHeightParent, mPaint);
        canvas.drawCircle(x + mWidthLine / 2, y, r, mPaint);
    }

    void setOther(Control other) {
        this.other = other;
    }

    void setX(int x) {
        if (mType == TYPE.HEAD) {
            if ((x + r) > (other.x - other.r)) {
                this.x = other.x - 2 * other.r;
                return;
            }
            if (x < bound.left) {
                this.x = bound.left;
                return;
            }
            if ((other.getCurrent() - this.getCurrent()) < MIN_DURATION) {
                return;
            }

        }
        if (mType == TYPE.TAIL) {
            if (x - r < (other.x + other.r)) {
                this.x = other.x + 2 * other.r;
                return;
            }
            if (x > bound.right) {
                this.x = bound.right;
                return;
            }
            if ((this.getCurrent() - other.getCurrent()) < MIN_DURATION) {
                return;
            }
        }
        this.x = x;
    }

    float getCurrent() {
        return (this.x - bound.left) * 1.0f / bound.width() * mMSDuration;
    }
}
