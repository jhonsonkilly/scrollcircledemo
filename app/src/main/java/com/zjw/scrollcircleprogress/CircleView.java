package com.zjw.scrollcircleprogress;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

import static com.zjw.scrollcircleprogress.SeekBarView.dpToPx;


/**
 * seekbar的背景的view
 * Created by mac on 19/5/20.
 * 根据滑动的距离移动dot
 * 提前在measure里做设置位置的操作
 */

public class CircleView extends View {


    protected int mOvalRadius;
    /**
     * 外层的粉色小圈
     */
    Paint mOuterPaint;
    /**
     * 中间的白色小圈
     */
    Paint mInnerPaint;
    /**
     * 高度
     */
    int measureHeight;
    /**
     * 间隔点的坐标
     */
    List<Point> mAllintervalPoints = new ArrayList<>();
    /**
     * 中心点坐标
     */
    private float centerX;

    private SeekBarView.ClickCallBack clickCallBack;
    /**
     * 父view
     */
    private SeekBarView seekBarView;


    public CircleView(Context context) {
        super(context);
        init();
    }

    public CircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    /**
     * 设置监听
     */
    public void setOnclickCallBackListener(SeekBarView.ClickCallBack clickCallBack) {
        this.clickCallBack = clickCallBack;
    }

    private void init() {
        mOuterPaint = new Paint();
        mOuterPaint.setAntiAlias(true);
        mOuterPaint.setColor(Color.parseColor("#FF7F71"));
        mOuterPaint.setStyle(Paint.Style.FILL);
        mInnerPaint = new Paint();
        mInnerPaint.setAntiAlias(true);
        mInnerPaint.setColor(Color.parseColor("#ffffff"));
        mInnerPaint.setStyle(Paint.Style.FILL);


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int lastX = 0;
        int x = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i("CircleView", getRight() + "");
                lastX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                int offsetX = x - lastX;
                if (getLeft() + offsetX >= 0 && getRight() + offsetX <= ((ViewGroup) getParent()).getMeasuredWidth()) {
                    ViewCompat.offsetLeftAndRight(this, offsetX);

                    seekBarView.getLineProgress(getLeft());
                }


                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                int finalX = getLeft();
                for (int i = 0; i < mAllintervalPoints.size(); i++) {
                    if (mAllintervalPoints.get(i).x >= finalX) {
                        try {
                            if (mAllintervalPoints.get(i - 1).x + centerX >= finalX) {
                                layout(mAllintervalPoints.get(i - 1).x, getTop(), mAllintervalPoints.get(i - 1).x + 2 * mOvalRadius, getBottom());
                                seekBarView.getCircleProgress(mAllintervalPoints.get(i - 1).x);
                                seekBarView.getLineProgress(mAllintervalPoints.get(i - 1).x);
                                dispatchListener(i - 1);
                                break;

                            } else {
                                layout(mAllintervalPoints.get(i).x, getTop(), mAllintervalPoints.get(i).x + 2 * mOvalRadius, getBottom());
                                seekBarView.getCircleProgress(mAllintervalPoints.get(i).x);
                                seekBarView.getLineProgress(mAllintervalPoints.get(i).x);
                                dispatchListener(i);
                                break;
                            }
                        } catch (Exception e) {
                            layout(mAllintervalPoints.get(i).x, getTop(), mAllintervalPoints.get(i).x + 2 * mOvalRadius, getBottom());
                            seekBarView.getCircleProgress(mAllintervalPoints.get(i).x);
                            seekBarView.getLineProgress(mAllintervalPoints.get(i).x);
                            dispatchListener(i);
                            break;
                        }
                    }
                }


                break;

        }
        return true;
    }

    private void dispatchListener(final int position) {
        if (clickCallBack != null) {
            clickCallBack.getSelectedItem(position);
        }
    }

    /**
     * 小球模拟到某个位置
     */
    public void simulateScroll(Point point) {
        layout(point.x, getTop(), point.x + 2 * mOvalRadius, getBottom());
        for (int i = 0; i < mAllintervalPoints.size(); i++) {
            if (mAllintervalPoints.get(i).x == point.x) {
                seekBarView.getCircleProgress(point.x);
                seekBarView.getLineProgress(point.x);
                dispatchListener(i);
                break;
            }
        }
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mOvalRadius, measureHeight / 2, mOvalRadius, mOuterPaint);
        canvas.drawCircle(mOvalRadius, measureHeight / 2, mOvalRadius / 3, mInnerPaint);

    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAllintervalPoints.clear();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(dpToPx(20), dpToPx(20));
        if (getParent() instanceof SeekBarView) {
            seekBarView = (SeekBarView) getParent();
        } else {
            throw new RuntimeException("this view parent must be seekBarView");
        }
        measureHeight = getMeasuredHeight();

        try {
            mOvalRadius = seekBarView.mOvalRadius;
            mAllintervalPoints = seekBarView.mAllintervalPoints;
            centerX = (mAllintervalPoints.get(1).x - mAllintervalPoints.get(0).x) / 2;
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
            params.setMargins(mAllintervalPoints.get(1).x, 0, 0, 0);
            setLayoutParams(params);
            seekBarView.getCircleProgress(mAllintervalPoints.get(mAllintervalPoints.size() - 2).x);
            seekBarView.getLineProgress(getLeft());
            Log.i("CircleView", measureHeight + "");
        } catch (Exception e) {
            e.printStackTrace();
        }



    }


}
