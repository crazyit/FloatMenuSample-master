/*
 * Copyright (c) 2016, Shanghai YUEWEN Information Technology Co., Ltd.
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  Neither the name of Shanghai YUEWEN Information Technology Co., Ltd. nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY SHANGHAI YUEWEN INFORMATION TECHNOLOGY CO., LTD. AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.yw.game.floatmenu;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;


/**
 * Created by wengyiming on 2017/7/21.
 */

/**
 * 00%=FF（不透明）    5%=F2    10%=E5    15%=D8    20%=CC    25%=BF    30%=B2    35%=A5    40%=99    45%=8c    50%=7F
 * 55%=72    60%=66    65%=59    70%=4c    75%=3F    80%=33    85%=21    90%=19    95%=0c    100%=00（全透明）
 */
public class DotImageView extends View {
    private static final String TAG = DotImageView.class.getSimpleName();
    public static final int NORMAL = 0;//不隐藏
    public static final int HIDE_LEFT = 1;//左边隐藏
    public static final int HIDE_RIGHT = 2;//右边隐藏
    private Paint mPaint;//用于画anything

    private Paint mPaintBg;//用于画anything
    private String dotNum = null;//红点数字
    private float mAlphaValue;//透明度动画值
    private float mRotateValue = 1f;//旋转动画值
    private boolean inited = false;//标记透明动画是否执行过，防止因onreseme 切换导致重复执行


    private Bitmap mBitmap;//logo
    private Bitmap mBitmapLogoBg;//logo
    private final int mLogoBackgroundRadius = dip2px(35);//logo的灰色背景圆的半径
    private final int mLogoWhiteRadius = dip2px(30);//logo的白色背景的圆的半径
    private final int mRedPointRadiusWithNum = dip2px(6);//红点圆半径
    private final int mRedPointRadius = dip2px(3);//红点圆半径
    private final int mRedPointOffset = dip2px(10);//红点对logo的偏移量，比如左红点就是logo中心的 x - mRedPointOffset

    private boolean isDrag = false;//是否 绘制旋转放大动画，只有 非停靠边缘才绘制
    private float scaleOffset;//放大偏移值
    private ValueAnimator mDragValueAnimator;//放大、旋转 属性动画
    private LinearInterpolator mLinearInterpolator = new LinearInterpolator();//通用用加速器
    public boolean mDrawDarkBg = true;//是否绘制黑色背景，当菜单关闭时，才绘制灰色背景
    private static final float hideOffset = 0.1f;//往左右隐藏多少宽度的偏移值， 隐藏宽度的0.4
    private Camera mCamera;//camera用于执行3D动画

    private boolean mDrawNum = false;//只绘制红点还是红点+白色数字

    private int mStatus = NORMAL;//0 正常，1 左，2右,3 中间方法旋转
    private int mLastStatus = mStatus;
    private Matrix mMatrix;
    private boolean mIsResetPosition;

    private int mBgColor = 0x99000000;
    private boolean drawTextProgress = false;
    private int progressWidthOffset = 25;

    /**
     * 画笔对象的引用
     */
    private Paint paint;

    /**
     * 圆环的颜色
     */
    private int ringColor;
    /**
     * 中间圆的颜色
     */
    private int circleColor;
    /**
     * 圆环进度的颜色
     */
    private int progressColor;

    /**
     * 中间进度百分比的字符串的颜色
     */
    private int textColor;

    /**
     * 中间进度百分比的字符串的字体
     */
    private float textSize;

    /**
     * 最大进度
     */
    private int max;

    /**
     * 当前进度
     */
    private float progress;
    /**
     * 是否显示中间的进度
     */
    private boolean textIsVisibility;

    /**
     * 进度的风格，实心或者空心
     */
    private int style;

    public static final int STROKE = 0;
    public static final int FILL = 1;

    private boolean rotateEnabled = false;
    private boolean dragScaleEnabled = false;
    private boolean drawBgCircleEnabled = false;

    public void setBgColor(int bgColor) {
        mBgColor = bgColor;
    }


    public void setDrawNum(boolean drawNum) {
        this.mDrawNum = drawNum;
    }

    public void setDrawDarkBg(boolean drawDarkBg) {
        mDrawDarkBg = drawDarkBg;
        invalidate();
    }

    public int getStatus() {
        return mStatus;
    }


    public void setStatus(int status) {
        this.mStatus = status;
        isDrag = false;
        if (this.mStatus != NORMAL) {
            setDrawNum(mDrawNum);
            this.mDrawDarkBg = true;
        }
        invalidate();

    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }
    public void setmBitmapLogoBg(Bitmap bitmap) {
        mBitmapLogoBg = bitmap;
    }

    public DotImageView(Context context, Bitmap bitmap,Bitmap bitmapLogoBg) {
        super(context);
        this.mBitmap = bitmap;
        this.mBitmapLogoBg = bitmapLogoBg;
        init();
        initProgress(context,null,0);
    }


    public DotImageView(Context context) {
        super(context);
        init();
        initProgress(context,null,0);
    }

    public DotImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        initProgress(context,attrs,0);
    }

    public DotImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initProgress(context,attrs,defStyleAttr);
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(sp2px(10));
        mPaint.setStyle(Paint.Style.FILL);

        mPaintBg = new Paint();
        mPaintBg.setAntiAlias(true);
        mPaintBg.setStyle(Paint.Style.FILL);
        mPaintBg.setColor(mBgColor);//60% 黑色背景 （透明度 40%）

        mCamera = new Camera();
        mMatrix = new Matrix();

    }
    private void initProgress(Context context, AttributeSet attrs, int defStyle){
        paint = new Paint();
        paint.setAntiAlias(true);//消除锯齿
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RingProgressBar);
        //获取自定义属性和默认值
        max = mTypedArray.getInteger(R.styleable.RingProgressBar_rMax, 100);//进度最大值
        textColor = mTypedArray.getColor(R.styleable.RingProgressBar_rTextColor, Color.GREEN);//字体颜色
        textSize = mTypedArray.getDimension(R.styleable.RingProgressBar_rTextSize, 40);//字体大小
        circleColor = mTypedArray.getColor(R.styleable.RingProgressBar_rCircleColor, Color.TRANSPARENT);//中间圆的颜色,默认透明
        ringColor = mTypedArray.getColor(R.styleable.RingProgressBar_rRingColor, Color.TRANSPARENT);//外层初始圆环的颜色，默认透明
        progressColor = mTypedArray.getColor(R.styleable.RingProgressBar_rProgressColor, 0xFFD93A26);//外层进度环的颜色，默认淡黄色
        textIsVisibility = mTypedArray.getBoolean(R.styleable.RingProgressBar_rTextIsVisibility, true);//文字是否可见，默认可见
        style = mTypedArray.getInt(R.styleable.RingProgressBar_style, 0);
        mTypedArray.recycle();
    }

    /**
     * 这个方法是否有优化空间
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int wh = mLogoBackgroundRadius * 2;
        setMeasuredDimension(wh, wh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;
        canvas.save();//保存一份快照，方便后面恢复
        mCamera.save();
        if (mStatus == NORMAL) {
            if (mLastStatus != NORMAL) {
                canvas.restore();//恢复画布的原始快照
                mCamera.restore();
            }

            if (isDrag) {
                //如果当前是拖动状态则放大并旋转
                if(dragScaleEnabled) {
                    canvas.scale((scaleOffset + 1f), (scaleOffset + 1f), getWidth() / 2, getHeight() / 2);
                }
                if (mIsResetPosition) {
                    //手指拖动后离开屏幕复位时使用 x轴旋转 3d动画
                    mCamera.save();
                    mCamera.rotateX(720 * scaleOffset);//0-720度 最多转两圈
                    mCamera.getMatrix(mMatrix);

                    mMatrix.preTranslate(-getWidth() / 2, -getHeight() / 2);
                    mMatrix.postTranslate(getWidth() / 2, getHeight() / 2);
                    canvas.concat(mMatrix);
                    mCamera.restore();
                } else {
                    //手指拖动且手指未离开屏幕则使用 绕图心2d旋转动画
                    if(this.rotateEnabled) {
                        canvas.rotate(60 * mRotateValue, getWidth() / 2, getHeight() / 2);
                    }
                }
            }


        } else if (mStatus == HIDE_LEFT) {
            canvas.translate(-getWidth() * hideOffset, 0);
            if(this.rotateEnabled){
                canvas.rotate(-45, getWidth() / 2, getHeight() / 2);
            }

        } else if (mStatus == HIDE_RIGHT) {
            canvas.translate(getWidth() * hideOffset, 0);
            if(this.rotateEnabled) {
                canvas.rotate(45, getWidth() / 2, getHeight() / 2);
            }
        }
        canvas.save();
        if (!isDrag) {
            if (mDrawDarkBg) {
                mPaintBg.setColor(mBgColor);
                if(drawBgCircleEnabled) {
                    canvas.drawCircle(centerX, centerY, mLogoBackgroundRadius, mPaintBg);
                }
                // 60% 白色 （透明度 40%）
                mPaint.setColor(0x99ffffff);
            } else {
                //100% 白色背景 （透明度 0%）
                mPaint.setColor(0xFFFFFFFF);
            }
            if (mAlphaValue != 0) {
                mPaint.setAlpha((int) (mAlphaValue * 255));
            }
            if(drawBgCircleEnabled) {
                canvas.drawCircle(centerX, centerY, mLogoWhiteRadius, mPaint);
            }
        }

        canvas.restore();
        //100% 白色背景 （透明度 0%）
        mPaint.setColor(0xFFFFFFFF);
        int left;
        int top;
        if(null != mBitmapLogoBg){
            left = (int) (centerX - mBitmapLogoBg.getWidth() / 2);
            top = (int) (centerY - mBitmapLogoBg.getHeight() / 2);
            canvas.drawBitmap(mBitmapLogoBg, left, top, mPaint);
        }
        left = (int) (centerX - mBitmap.getWidth() / 2);
        top = (int) (centerY - mBitmap.getHeight() / 2);

        canvas.drawBitmap(mBitmap, left, top, mPaint);


        if (!TextUtils.isEmpty(dotNum)) {
            int readPointRadus = (mDrawNum ? mRedPointRadiusWithNum : mRedPointRadius);
            mPaint.setColor(Color.RED);
            if (mStatus == HIDE_LEFT) {
                if(drawBgCircleEnabled) {
                    canvas.drawCircle(centerX + mRedPointOffset, centerY - mRedPointOffset, readPointRadus, mPaint);
                }
                if (mDrawNum) {
                    mPaint.setColor(Color.WHITE);
                    canvas.drawText(dotNum, centerX + mRedPointOffset - getTextWidth(dotNum, mPaint) / 2, centerY - mRedPointOffset + getTextHeight(dotNum, mPaint) / 2, mPaint);
                }
            } else if (mStatus == HIDE_RIGHT) {
                if(drawBgCircleEnabled) {
                    canvas.drawCircle(centerX - mRedPointOffset, centerY - mRedPointOffset, readPointRadus, mPaint);
                }
                if (mDrawNum) {
                    mPaint.setColor(Color.WHITE);
                    canvas.drawText(dotNum, centerX - mRedPointOffset - getTextWidth(dotNum, mPaint) / 2, centerY - mRedPointOffset + getTextHeight(dotNum, mPaint) / 2, mPaint);
                }
            } else {
                if (mLastStatus == HIDE_LEFT) {
                    if(drawBgCircleEnabled) {
                        canvas.drawCircle(centerX + mRedPointOffset, centerY - mRedPointOffset, readPointRadus, mPaint);
                    }
                    if (mDrawNum) {
                        mPaint.setColor(Color.WHITE);
                        canvas.drawText(dotNum, centerX + mRedPointOffset - getTextWidth(dotNum, mPaint) / 2, centerY - mRedPointOffset + getTextHeight(dotNum, mPaint) / 2, mPaint);
                    }
                } else if (mLastStatus == HIDE_RIGHT) {
                    if(drawBgCircleEnabled) {
                        canvas.drawCircle(centerX - mRedPointOffset, centerY - mRedPointOffset, readPointRadus, mPaint);
                    }
                    if (mDrawNum) {
                        mPaint.setColor(Color.WHITE);
                        canvas.drawText(dotNum, centerX - mRedPointOffset - getTextWidth(dotNum, mPaint) / 2, centerY - mRedPointOffset + getTextHeight(dotNum, mPaint) / 2, mPaint);
                    }
                }
            }
        }
        mLastStatus = mStatus;
        if (isDrag) {
            try {
                canvas.save();
                canvas.restore();
            }catch (Exception e){
                Log.e("error",""+e);
            }
        }
        //画进度
        int ringWidth = getWidth() / 6;
        //1.画最外层的圆环
        int centre = getWidth() / 2; //获取圆心的x坐标
        int radius = (int) (centre - ringWidth / 2); //圆环的半径
        paint.setStrokeWidth(ringWidth); //设置圆环的宽度
        paint.setColor(ringColor); //设置圆环的颜色
        paint.setStyle(Paint.Style.STROKE); //设置空心
        canvas.drawCircle(centre, centre, radius, paint); //画出圆环
        //2.画中间圆环
        paint.setColor(circleColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centre, centre, centre - ringWidth, paint); //画出圆环
        //3.画进度圆环
        paint.setStrokeWidth(ringWidth- progressWidthOffset); //设置圆环的宽度
        paint.setColor(progressColor); //设置进度的颜色
        RectF oval = new RectF(centre - radius, centre - radius, centre + radius, centre + radius); //用于定义的圆弧的形状和大小的界限
        switch (style) {
            case STROKE: {//空心进度环
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawArc(oval, -90, 360 * progress / max, false, paint);  //根据进度画圆弧
                break;
            }
            case FILL: {//实心进度环
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                if (progress != 0) {
                    canvas.drawArc(oval, -90, 360 * progress / max, true, paint);  //根据进度画圆弧
                }
                break;
            }
        }
        //4.画进度百分比
        if (textIsVisibility && drawTextProgress) {
            paint.setStrokeWidth(0);
            paint.setColor(textColor);
            paint.setTextSize(textSize);
            paint.setTypeface(Typeface.DEFAULT_BOLD); //设置字体
            int percent = (int) (((float) progress / (float) max) * 100); //中间的进度百分比，先转换成float在进行除法运算，不然都为0
            float textWidth = paint.measureText(percent + "%");   //测量字体宽度，我们需要根据字体的宽度设置在圆环中间
            canvas.drawText(percent + "%", centre - textWidth / 2, centre + textSize / 2, paint); //画出进度百分比
        }
    }
    public void setDrawTextProgress(boolean drawTextProgress){
        this.drawTextProgress = drawTextProgress;
    }
    /**
     * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
     * 刷新界面调用postInvalidate()能在非UI线程刷新
     *
     * @param progress
     */
    public synchronized void setProgress(float progress,float time) {
        if (progress < 0) {
            throw new IllegalArgumentException("progress not less than 0");
        }
        if (progress > max) {
            progress = max;
        }
        if (progress <= max) {
//            this.progress = progress;
            setProgressAnimation(this.progress,this.progress,progress,time);
//            postInvalidate();
        }

    }
    /**
     * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
     * 刷新界面调用postInvalidate()能在非UI线程刷新
     *
     * @param progress
     */
    public synchronized void setProgressWithoutAnimation(float progress) {
        if (progress < 0) {
            throw new IllegalArgumentException("progress not less than 0");
        }
        if (progress > max) {
            progress = max;
        }
        if (progress <= max) {
            this.progress = progress;
            postInvalidate();
        }

    }
    private void setProgressAnimation(float startAngle, float currentAngle,float currentValue, float time){
        //绘制当前数据对应的圆弧的动画效果
        ValueAnimator progressAnimator = ValueAnimator.ofFloat(startAngle, currentValue);
        progressAnimator.setDuration((long) time);
        progressAnimator.setTarget(this.progress);
//        progressAnimator.setInterpolator(new AccelerateInterpolator());
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progress = (float) animation.getAnimatedValue();
                //重新绘制，不然不会出现效果
                postInvalidate();
            }
        });
        //开始执行动画
        progressAnimator.start();
    }
    public synchronized void setRotateEnabled(boolean rotateEnabled) {
        this.rotateEnabled = rotateEnabled;
    }
    public synchronized void setDragScaleEnabled(boolean dragScaleEnabled) {
        this.dragScaleEnabled = dragScaleEnabled;
    }
    public synchronized void setDrawBgCircleEnabled(boolean drawBgCircleEnabled) {
        this.drawBgCircleEnabled = drawBgCircleEnabled;
    }
    public synchronized void setProgressWidthOffset(int progressWidthOffset) {
        this.progressWidthOffset = progressWidthOffset;
    }
    public void setDotNum(int num, Animator.AnimatorListener l) {
        if (!inited) {
            startAnim(num, l);
        } else {
            refreshDot(num);
        }
    }

    private void refreshDot(int num) {
        if (num > 0) {
            String dotNumTmp = String.valueOf(num);
            if (!TextUtils.equals(dotNum, dotNumTmp)) {
                dotNum = dotNumTmp;
                invalidate();
            }
        } else {
            dotNum = null;
        }
    }


    public void startAnim(final int num, Animator.AnimatorListener l) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1.f, 0.6f, 1f, 0.6f);
        valueAnimator.setInterpolator(mLinearInterpolator);
        valueAnimator.setDuration(3000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAlphaValue = (float) animation.getAnimatedValue();
                invalidate();

            }
        });
        valueAnimator.addListener(l);
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                inited = true;
                refreshDot(num);
                mAlphaValue = 0;

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mAlphaValue = 0;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
    }

    public void setDrag(boolean drag, float offset, boolean isResetPosition) {
        isDrag = drag;
        this.mIsResetPosition = isResetPosition;
        if (offset > 0 && offset != this.scaleOffset) {
            this.scaleOffset = offset;
        }
        if (isDrag && mStatus == NORMAL) {
            if (mDragValueAnimator != null) {
                if (mDragValueAnimator.isRunning()) return;
            }
            mDragValueAnimator = ValueAnimator.ofFloat(0, 6f, 12f, 0f);
            mDragValueAnimator.setInterpolator(mLinearInterpolator);
            mDragValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mRotateValue = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mDragValueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isDrag = false;
                    mIsResetPosition = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mDragValueAnimator.setDuration(1000);
            mDragValueAnimator.start();
        }
    }

    private int dip2px(float dipValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private float getTextHeight(String text, Paint paint) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.height() / 1.1f;
    }

    private float getTextWidth(String text, Paint paint) {
        return paint.measureText(text);
    }
}
