package com.demo.board;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class VerticalPointerSave extends View {
    private float mOuterRaidus;//最外环半径
    private float mRadius; //外圆半径

    private float mPadding; //边距
    private float mTextSize; //文字大小

    private int mColorLong; //长线的颜色
    private int mColorShort; //短线的颜色
    private Paint mPaint; //画笔
    private float mSpace = DptoPx(20);//刻度尺与圆盘的上下间隔
    private float mTextScalePadding = DptoPx(8);//刻度值与刻度尺的间隔
    private float translateY/*= -mRadius + mSpace*/;//绘制刻度时圆心移动的Y轴方向绝对值
    private float drawHeight/* = 2 * (mRadius - mSpace)*/;//划线的高高度
    private float translateX/* = mRadius / 20*/;//绘制刻度时圆心移动的X轴方向绝对值
    private float scaleTranslateX /*= mRadius / 4*/;//绘制刻度尺时，刻度尺向X轴偏离的数值，左边的刻度尺向左偏移则为负值，右边的向右偏移则为正值
    private float mDegree = 0;

    public VerticalPointerSave(Context context) {
        this(context, null);
    }

    public VerticalPointerSave(Context context, AttributeSet attrs) {
        super(context, attrs);
        obtainStyledAttrs(attrs); //获取自定义的属性
        init(); //初始化画笔
    }

    private void obtainStyledAttrs(AttributeSet attrs) {
        TypedArray array = null;
        try {
            array = getContext().obtainStyledAttributes(attrs, R.styleable.VerticalPointer);
            mPadding = array.getDimension(R.styleable.VerticalPointer_vp_padding, DptoPx(5));
            mTextSize = array.getDimension(R.styleable.VerticalPointer_vp_text_size, SptoPx(9));
            mColorLong = array.getColor(R.styleable.VerticalPointer_vp_scale_long_color, Color.argb(225, 0, 0, 0));
            mColorShort = array.getColor(R.styleable.VerticalPointer_vp_scale_short_color, Color.argb(125, 0, 0, 0));
        } catch (Exception e) {
            //一旦出现错误全部使用默认值
            mPadding = DptoPx(5);
            mTextSize = SptoPx(2);
            mColorLong = Color.argb(225, 0, 0, 0);
            mColorShort = Color.argb(125, 0, 0, 0);
        } finally {
            if (array != null) {
                array.recycle();
            }
        }
    }

    //Dp转px
    private float DptoPx(int value) {

        return SizeUtil.Dp2Px(getContext(), value);
    }

    //sp转px
    private float SptoPx(int value) {
        return SizeUtil.Sp2Px(getContext(), value);
    }

    //画笔初始化
    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = 1000; //设定一个最小值
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED || heightMeasureSpec == MeasureSpec.AT_MOST || heightMeasureSpec == MeasureSpec.UNSPECIFIED) {
            try {
                throw new NoDetermineSizeException("宽度高度至少有一个确定的值,不能同时为wrap_content");
            } catch (NoDetermineSizeException e) {
                e.printStackTrace();
            }
        } else { //至少有一个为确定值,要获取其中的最小值
            if (widthMode == MeasureSpec.EXACTLY) {
                width = Math.min(widthSize, width);
            }
            if (heightMode == MeasureSpec.EXACTLY) {
                width = Math.min(heightSize, width);
            }
        }
        setMeasuredDimension(width, width);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mOuterRaidus = (Math.min(w, h) - getPaddingLeft() - getPaddingRight()) / 2;//最外层半径
        mRadius = mOuterRaidus * 12 / 13;//实际可用半径
        translateY = -mRadius + mSpace;//绘制刻度时圆心移动的Y轴方向绝对值
        drawHeight = 2 * (mRadius - mSpace);//划线的高高度
        translateX = mRadius / 20;//绘制刻度时圆心移动的X轴方向绝对值
        scaleTranslateX = mRadius / 4;//绘制刻度尺时，刻度尺向X轴偏离的数值，左边的刻度尺向左偏移则为负值，右边的向右偏移则为正值
    }

    //绘制外圆背景
    public void paintCircle(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(getContext().getResources().getColor(R.color.color_black_mask_dark));
        canvas.drawCircle(0, 0, mOuterRaidus, mPaint);
        mPaint.setColor(getContext().getResources().getColor(R.color.color_black_mask));
        canvas.drawCircle(0, 0, mRadius, mPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(getWidth() / 2, getHeight() / 2);
        //绘制外圆背景
        paintCircle(canvas);
        //绘制刻度
        paintScale(canvas);
        paintMask(canvas);
        paintText(canvas);
        canvas.restore();
    }

    //绘制刻度
    private void paintScale(Canvas canvas) {
        int lineWidth = 20;//刻度尺长度
        int longLineWidth = 6;//关键位刻度尺比普通刻度尺的多余长度
        int scaleCount = 37;//刻度尺的刻度数量
        float singleScaleSpace = drawHeight / (scaleCount - 1);//单个刻度尺标的间隔高度
        //绘制左边的刻度
        canvas.save();
        canvas.translate(-translateX, translateY);
        float translateScale = mDegree * drawHeight / 180;//垂直移动距离，如果为正数，则表盘刻度向下移动，为负反之
        for (int i = 0; i < scaleCount; i++) {
            String text = "";
            if (i == 3) {
                text = "75";
                mPaint.setColor(mColorLong);
                mPaint.setTextSize(mTextSize);
                mPaint.setStrokeWidth(SizeUtil.Dp2Px(getContext(), 1.5f));
                Rect textBound = new Rect();
                mPaint.getTextBounds(text, 0, text.length(), textBound);
                canvas.drawText(text, -scaleTranslateX - textBound.width() - mTextScalePadding, i * singleScaleSpace + textBound.height() / 2, mPaint);
                canvas.drawLine(-scaleTranslateX - longLineWidth, i * singleScaleSpace, -scaleTranslateX + lineWidth, i * singleScaleSpace, mPaint);

            } else if (i > 4 && ((i - 3) % 5 == 0)) {
                text = (90 - (5 * i)) + "";
                if ((90 - (5 * i)) == 0) {
                    mPaint.setColor(getContext().getResources().getColor(R.color.color_red_half));
                } else {
                    mPaint.setColor(mColorLong);
                }
                mPaint.setTextSize(mTextSize);
                mPaint.setStrokeWidth(SizeUtil.Dp2Px(getContext(), 1.5f));
                Rect textBound = new Rect();
                mPaint.getTextBounds(text, 0, text.length(), textBound);
                canvas.drawText(text, -scaleTranslateX - textBound.width() - mTextScalePadding, i * singleScaleSpace + textBound.height() / 2, mPaint);
                canvas.drawLine(-scaleTranslateX - longLineWidth, i * singleScaleSpace, -scaleTranslateX + lineWidth, i * singleScaleSpace, mPaint);

            } else {
                mPaint.setStrokeWidth(SizeUtil.Dp2Px(getContext(), 1f));
                mPaint.setColor(mColorShort);
                canvas.drawLine(-scaleTranslateX, i * singleScaleSpace, -scaleTranslateX + lineWidth, i * singleScaleSpace, mPaint);
            }
        }
        canvas.restore();
        //绘制右边的刻度
        canvas.save();
        canvas.translate(translateX, translateY);
        for (int i = 0; i < scaleCount; i++) {
            String text = "";
            if (i == 3) {
                text = "75";
                mPaint.setColor(mColorLong);
                mPaint.setTextSize(mTextSize);
                Rect textBound = new Rect();
                mPaint.setStrokeWidth(SizeUtil.Dp2Px(getContext(), 1.5f));
                mPaint.getTextBounds(text, 0, text.length(), textBound);
                canvas.drawText(text, scaleTranslateX + mTextScalePadding, i * singleScaleSpace + textBound.height() / 2, mPaint);
                canvas.drawLine(scaleTranslateX + longLineWidth, i * singleScaleSpace, scaleTranslateX - lineWidth, i * singleScaleSpace, mPaint);
            } else if (i > 4 && ((i - 3) % 5 == 0)) {
                text = (90 - (5 * i)) + "";
                if ((90 - (5 * i)) == 0) {
                    mPaint.setColor(getContext().getResources().getColor(R.color.color_red_half));
                } else {
                    mPaint.setColor(mColorLong);
                }
                mPaint.setTextSize(mTextSize);
                mPaint.setStrokeWidth(SizeUtil.Dp2Px(getContext(), 1.5f));
                Rect textBound = new Rect();
                mPaint.getTextBounds(text, 0, text.length(), textBound);
                canvas.drawText(text, scaleTranslateX + mTextScalePadding, i * singleScaleSpace + textBound.height() / 2, mPaint);
                canvas.drawLine(scaleTranslateX + longLineWidth, i * singleScaleSpace, scaleTranslateX - lineWidth, i * singleScaleSpace, mPaint);
            } else {
                mPaint.setStrokeWidth(SizeUtil.Dp2Px(getContext(), 1f));
                mPaint.setColor(mColorShort);
                canvas.drawLine(scaleTranslateX, i * singleScaleSpace, scaleTranslateX - lineWidth, i * singleScaleSpace, mPaint);
            }
        }
        canvas.restore();
    }

    private void paintMask(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(getContext().getResources().getColor(R.color.color_red_mask));
        Rect rect = new Rect((int) Math.ceil(-scaleTranslateX), 0, (int) Math.ceil(scaleTranslateX), (int) Math.ceil(drawHeight / 2));
        canvas.drawRect(rect, mPaint);
    }

    private void paintText(Canvas canvas) {
        mPaint.setColor(mColorLong);
        String textTian = "天";
        Rect textTianBound = new Rect();
        mPaint.setStrokeWidth(SizeUtil.Dp2Px(getContext(), 1.5f));
        mPaint.getTextBounds(textTian, 0, textTian.length(), textTianBound);
        mPaint.setTextSize(mTextSize + 3);
        canvas.drawText(textTian, 0 - (textTianBound.width() / 2), (-drawHeight / 2) - (textTianBound.height() / 2f), mPaint);

        String textDi = "地";
        Rect textDiBound = new Rect();
        mPaint.setStrokeWidth(SizeUtil.Dp2Px(getContext(), 1.5f));
        mPaint.getTextBounds(textDi, 0, textDi.length(), textDiBound);
        mPaint.setTextSize(mTextSize + 3);
        canvas.drawText(textDi, 0 - (textDiBound.width() / 2), (drawHeight / 2) + (textDiBound.height() * 1.5f), mPaint);
    }

    public void changeDegree(float degree) {
        if (mDegree == degree) {
            return;
        }
        this.mDegree = degree;
        invalidate();
    }

    class NoDetermineSizeException extends Exception {
        public NoDetermineSizeException(String message) {
            super(message);
        }
    }
}
