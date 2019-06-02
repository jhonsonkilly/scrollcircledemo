package com.zjw.scrollcircleprogress;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * seekbar的背景的viewgroup
 * Created by mac on 19/5/20.
 * ui给的背景
 */

public class SeekBarView extends FrameLayout {
    /**
     * 默认间隔
     */
    private static final int defaultNumber = 6;
    /**
     * 默认最小点击范围(像素)
     */
    private static final int defaultMinInternal = 10;
    /**
     * 默认最小点击范围(像素)
     */
    private static final int defaultRectNumber = 50;
    /**
     * 小球半径
     */
    public int mOvalRadius;

    /**
     * 背景的paint
     */
    private Paint mBackGroundPaint;
    /**
     * 进度条的paint
     */
    private Paint mProgressPaint;
    /**
     * 宽度
     */
    private int measureWidth;
    /**
     * 高度
     */
    private int measureHeight;
    /**
     * 间隔点的坐标
     */
    public List<Point> mAllintervalPoints = new ArrayList<>();


    private int internalNumber = defaultNumber;

    private CircleView mCircleView;
    /**
     * 重置的小球半径
     */
    private int resetRadius = -1;

    /**
     * 进度条的小球
     */
    private int progressMeasureWidth = -1;

    /**
     * 进度条的线
     */
    private int progressMeasureLine = -1;


    public SeekBarView(Context context) {
        super(context);
        init(context);
    }

    public SeekBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SeekBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public interface ClickCallBack {
        //选中的回调;
        void getSelectedItem(int position);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        mBackGroundPaint = new Paint();
        mBackGroundPaint.setAntiAlias(true);
        mBackGroundPaint.setColor(Color.parseColor("#D0D0D0"));
        mBackGroundPaint.setStrokeWidth(10);
        mBackGroundPaint.setStyle(Paint.Style.FILL);
        mCircleView = new CircleView(context);
        addView(mCircleView);

        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setColor(Color.parseColor("#FF7F71"));
        mProgressPaint.setStrokeWidth(10);
        mProgressPaint.setStrokeJoin(Paint.Join.ROUND);
        mProgressPaint.setStyle(Paint.Style.FILL);


    }

    public void setOnclickCallBackListener(ClickCallBack clickCallBack) {
        mCircleView.setOnclickCallBackListener(clickCallBack);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        float halfHeight = measureHeight / 2;
        int eachWidth = (measureWidth - 2 * mOvalRadius) / internalNumber;
        canvas.save();
        canvas.translate(mOvalRadius, 0);
        int totalPaintWidth = 0;


        while (totalPaintWidth <= measureWidth) {
            //canvas.drawLine(totalPaintWidth, -halfHeight / 2, totalPaintWidth, halfHeight / 2, mBackGroundPaint);
            canvas.drawCircle(totalPaintWidth, halfHeight, halfHeight / 2, mBackGroundPaint);
            totalPaintWidth = totalPaintWidth + eachWidth;

        }
        canvas.restore();
        canvas.drawLine(mOvalRadius, measureHeight / 2, measureWidth - mOvalRadius, measureHeight / 2, mBackGroundPaint);
        canvas.save();
        canvas.translate(mOvalRadius, 0);
        int progressPaintWidth = 0;

        for (int i = 0; i < mAllintervalPoints.size(); i++) {
            if (progressPaintWidth != -1 && mAllintervalPoints.get(i).x >= progressMeasureLine) {
                try {
                    progressMeasureWidth = mAllintervalPoints.get(i - 1).x;
                    break;
                } catch (Exception e) {
                    progressMeasureWidth = mAllintervalPoints.get(i).x;
                }

            }
        }
        while (progressPaintWidth <= progressMeasureWidth) {
            //canvas.drawLine(totalPaintWidth, -halfHeight / 2, totalPaintWidth, halfHeight / 2, mBackGroundPaint);
            canvas.drawCircle(progressPaintWidth, halfHeight, halfHeight / 2, mProgressPaint);
            progressPaintWidth = progressPaintWidth + eachWidth;

        }
        canvas.restore();
        canvas.drawLine(mOvalRadius, measureHeight / 2, progressMeasureLine + mOvalRadius, measureHeight / 2, mProgressPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int lastX = 0;
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if ((lastX - x) < Math.abs(defaultMinInternal)) {

                    Point finalPoint = isControlPoint(x, y);
                    if (finalPoint != null) {
                        if (getChildAt(0) instanceof CircleView) {
                            CircleView circleView = (CircleView) getChildAt(0);
                            circleView.simulateScroll(finalPoint);
                        } else {
                            throw new RuntimeException("the seekBarView must be one child");
                        }
                    }
                }

                break;
        }
        return true;

    }

    /**
     * 判断点击区域
     */
    private Point isControlPoint(float x, float y) {
        for (Point controlPoint : mAllintervalPoints) {
            RectF pointRange = new RectF(controlPoint.x - defaultRectNumber,
                    controlPoint.y - defaultRectNumber,
                    controlPoint.x + defaultRectNumber,
                    controlPoint.y + defaultRectNumber);
            // 如果包含了就，返回true
            if (pointRange.contains(x, y)) {
                return controlPoint;
            }

        }
        return null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (heightMode == MeasureSpec.AT_MOST) {
            measureHeight = dpToPx(20);
        }
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(measureHeight, MeasureSpec.EXACTLY);
        mOvalRadius = resetRadius == -1 ? measureHeight / 2 : resetRadius;

        int eachWidth = (measureWidth - 2 * mOvalRadius) / internalNumber;
        if (eachWidth > 0) {
            int totalPaintWidth = 0;
            mAllintervalPoints.clear();
            while (totalPaintWidth <= measureWidth) {
                mAllintervalPoints.add(new Point(totalPaintWidth, measureHeight / 2));
                totalPaintWidth = totalPaintWidth + eachWidth;

            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 设置小球半径
     */
    public void setOvalRadius(int radius) {
        resetRadius = radius;
        requestLayout();
        invalidate();
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAllintervalPoints.clear();
    }


    /**
     * 转换 dp 至 px
     *
     * @param dpValue dp值
     * @return px值
     */
    protected static int dpToPx(float dpValue) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) (dpValue * metrics.density + 0.5f);
    }

    /**
     * 设置间距
     */
    public void setIntervalNumber(int number) {
        internalNumber = Math.min(10, Math.max(1, number));
        requestLayout();
        invalidate();
    }

    /**
     * 重置状态
     */
    public void resetState() {
        internalNumber = defaultNumber;
        resetRadius = -1;
        requestLayout();
        invalidate();
    }

    /**
     * 设置进度条的圆点
     */
    protected void getCircleProgress(int progressMeasureWidth) {
        this.progressMeasureWidth = progressMeasureWidth;
        invalidate();
    }

    /**
     * 设置进度条的线
     */
    protected void getLineProgress(int progressMeasureLine) {
        this.progressMeasureLine = progressMeasureLine;
        invalidate();
    }


}
