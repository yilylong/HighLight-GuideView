package com.zhl.userguideview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


/**
 * 描述：一个用于“应用新特性”的用户指引view
 * Created by zhaohl on 2015-11-26.
 */
public class UserGuideView extends View {
    public static final int VIEWSTYLE_RECT = 0;
    public static final int VIEWSTYLE_CIRCLE = 1;
    public static final int VIEWSTYLE_OVAL = 2;
    public static final int MASKBLURSTYLE_SOLID = 0;
    public static final int MASKBLURSTYLE_NORMAL = 1;
    private Bitmap fgBitmap;// 前景
    private Bitmap jtUpLeft, jtUpRight, jtUpCenter, jtDownRight, jtDownLeft, jtDownCenter;// 指示箭头
    private Canvas mCanvas;// 绘制蒙版层的画布
    private Paint mPaint;// 绘制蒙版层画笔
    private int screenW, screenH;// 屏幕宽高
    private View targetView;
    private boolean touchOutsideCancel = true;
    private int borderWitdh = 10;// 边界余量
    private int offestMargin = 10;// 光圈放大偏移值
    private int margin = 10;
    private int highLightStyle = VIEWSTYLE_RECT;
    public int maskblurstyle = MASKBLURSTYLE_SOLID;
    private Bitmap tipBitmap;
    private int radius;
    private int maskColor = 0x99000000;// 蒙版层颜色
    private OnDismissListener onDismissListener;
    private int statusBarHeight = 0;// 状态栏高度
    private ArrayList<View> targetViews;
    private Rect tipViewHitRect;

    public UserGuideView(Context context) {
        this(context, null);
    }

    public UserGuideView(Context context, AttributeSet set) {
        this(context, set, -1);
    }

    public UserGuideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.UserGuideView);
            highLightStyle = array.getInt(R.styleable.UserGuideView_HighlightViewStyle, VIEWSTYLE_RECT);
            maskblurstyle = array.getInt(R.styleable.UserGuideView_MaskBlurStyle, MASKBLURSTYLE_SOLID);
            BitmapDrawable drawable = (BitmapDrawable) array.getDrawable(R.styleable.UserGuideView_tipView);
            maskColor = array.getColor(R.styleable.UserGuideView_maskColor, maskColor);
            if (drawable != null) {
                tipBitmap = drawable.getBitmap();
            }
            BitmapDrawable jtUpCenter = (BitmapDrawable) array.getDrawable(R.styleable.UserGuideView_indicator_arrow_up_center);
            BitmapDrawable jtUpLeft = (BitmapDrawable) array.getDrawable(R.styleable.UserGuideView_indicator_arrow_up_left);
            BitmapDrawable jtUpRight = (BitmapDrawable) array.getDrawable(R.styleable.UserGuideView_indicator_arrow_up_right);
            BitmapDrawable jtDownCenter = (BitmapDrawable) array.getDrawable(R.styleable.UserGuideView_indicator_arrow_down_center);
            BitmapDrawable jtDownLeft = (BitmapDrawable) array.getDrawable(R.styleable.UserGuideView_indicator_arrow_down_left);
            BitmapDrawable jtDownRight = (BitmapDrawable) array.getDrawable(R.styleable.UserGuideView_indicator_arrow_down_right);
            if (jtUpCenter != null) {
                this.jtUpCenter = jtUpCenter.getBitmap();
            } else {
                this.jtUpCenter = BitmapFactory.decodeResource(getResources(), R.drawable.jt_up_center);
            }
            if (jtUpLeft != null) {
                this.jtUpLeft = jtUpLeft.getBitmap();
            } else {
                this.jtUpLeft = BitmapFactory.decodeResource(getResources(), R.drawable.jt_up_left);
            }
            if (jtUpRight != null) {
                this.jtUpRight = jtUpRight.getBitmap();
            } else {
                this.jtUpRight = BitmapFactory.decodeResource(getResources(), R.drawable.jt_up_right);
            }
            if (jtDownCenter != null) {
                this.jtDownCenter = jtDownCenter.getBitmap();
            } else {
                this.jtDownCenter = BitmapFactory.decodeResource(getResources(), R.drawable.jt_down_center);
            }
            if (jtDownLeft != null) {
                this.jtDownLeft = jtDownLeft.getBitmap();
            } else {
                this.jtDownLeft = BitmapFactory.decodeResource(getResources(), R.drawable.jt_down_left);
            }
            if (jtDownRight != null) {
                this.jtDownRight = jtDownRight.getBitmap();
            } else {
                this.jtDownRight = BitmapFactory.decodeResource(getResources(), R.drawable.jt_down_right);
            }
            array.recycle();
        }
        // 计算参数
        cal(context);

        // 初始化对象
        init(context);
    }

    /**
     * 计算参数
     *
     * @param context
     */
    private void cal(Context context) {
        int[] screenSize = MeasureUtil.getScreenSize(context);
        screenW = screenSize[0];
        screenH = screenSize[1];
        Rect frame = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        statusBarHeight = frame.top;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            statusBarHeight = 44;
        }
    }

    /**
     * 初始化对象
     */
    private void init(Context context) {

//        setLayerType(LAYER_TYPE_SOFTWARE,null);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        mPaint.setARGB(0, 255, 0, 0);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        BlurMaskFilter.Blur blurStyle = null;
        switch (maskblurstyle) {
            case MASKBLURSTYLE_SOLID:
                blurStyle = BlurMaskFilter.Blur.SOLID;
                break;
            case MASKBLURSTYLE_NORMAL:
                blurStyle = BlurMaskFilter.Blur.NORMAL;
                break;
        }

        mPaint.setMaskFilter(new BlurMaskFilter(15, blurStyle));

        fgBitmap = MeasureUtil.createBitmapSafely(screenW, screenH, Bitmap.Config.ARGB_8888, 2);
        if (fgBitmap == null) {
            throw new RuntimeException("out of memery cause fgbitmap create fail");
        }
        mCanvas = new Canvas(fgBitmap);

        mCanvas.drawColor(maskColor);

//        jtDownRight = BitmapFactory.decodeResource(getResources(), R.drawable.jt_down_right);
//        jtDownLeft = BitmapFactory.decodeResource(getResources(), R.drawable.jt_down_left);
//        jtUpLeft = BitmapFactory.decodeResource(getResources(), R.drawable.jt_up_left);
//        jtUpRight = BitmapFactory.decodeResource(getResources(), R.drawable.jt_up_right);
//        jtDownCenter = BitmapFactory.decodeResource(getResources(), R.drawable.jt_down_center);
    }

    /**
     * set indicator arrow up center
     * @param resId
     */
    public void setArrowUpCenter(int resId){
        jtUpCenter = BitmapFactory.decodeResource(getResources(), resId);
    }
    /**
     * set indicator arrow up left
     * @param resId
     */
    public void setArrowUpLeft(int resId){
        jtUpLeft = BitmapFactory.decodeResource(getResources(), resId);
    }
    /**
     * set indicator arrow up right
     * @param resId
     */
    public void setArrowUpRight(int resId){
        jtUpRight = BitmapFactory.decodeResource(getResources(), resId);
    }
    /**
     * set indicator arrow down center
     * @param resId
     */
    public void setArrowDownCenter(int resId){
        jtDownCenter = BitmapFactory.decodeResource(getResources(), resId);
    }
    /**
     * set indicator arrow down left
     * @param resId
     */
    public void setArrowDownLeft(int resId){
        jtDownLeft = BitmapFactory.decodeResource(getResources(), resId);
    }
    /**
     * set indicator arrow down right
     * @param resId
     */
    public void setArrowDownRight(int resId){
        jtDownRight = BitmapFactory.decodeResource(getResources(), resId);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (targetView == null) {
            return;
        }
        canvas.drawBitmap(fgBitmap, 0, 0, null);
//        int left = targetView.getLeft();
//        int top = targetView.getTop();
        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;
        int vWidth = targetView.getWidth();
        int vHeight = targetView.getHeight();

        Rect tagetRect = new Rect();
        targetView.getGlobalVisibleRect(tagetRect);
        tagetRect.offset(0, -statusBarHeight);
        left = tagetRect.left - offestMargin;
        top = tagetRect.top - offestMargin;
        right = tagetRect.right + offestMargin;
        bottom = tagetRect.bottom + offestMargin;

        if (left == 0) {
            left += borderWitdh;
        } else if (top == 0) {
            top += borderWitdh;
        } else if (right == screenW) {
            right -= borderWitdh;
        } else if (bottom == screenH) {
            bottom -= borderWitdh;
        }
        switch (highLightStyle) {
            case VIEWSTYLE_RECT:
                RectF rect = new RectF(left, top, right, bottom);
                mCanvas.drawRoundRect(rect, 20, 20, mPaint);
                break;
            case VIEWSTYLE_CIRCLE:
                radius = vWidth < vHeight ? vWidth / 2 + 2 * offestMargin : vHeight / 2 + 2 * offestMargin;
                if (radius < 50) {
                    radius = 100;
                }
                mCanvas.drawCircle(left + offestMargin + vWidth / 2, top + offestMargin + vHeight / 2, radius, mPaint);
                break;
            case VIEWSTYLE_OVAL:
                RectF rectf = new RectF(left, top, right, bottom);
                mCanvas.drawOval(rectf, mPaint);
                break;

        }

        if (bottom < screenH / 2 || (screenH / 2 - top > bottom - screenH / 2)) {// top
            int jtTop = getUpJtTop(bottom, vHeight);
            if (right < screenW / 2 || (screenW / 2 - left > right - screenW / 2)) {//left
                canvas.drawBitmap(jtUpLeft, left + vWidth / 2, jtTop, null);
                if (tipBitmap != null) {
                    canvas.drawBitmap(tipBitmap, left + vWidth / 2, jtTop + jtUpLeft.getHeight(), null);
                    tipViewHitRect = new Rect(left + vWidth / 2,jtTop + jtUpLeft.getHeight(),left + vWidth / 2+tipBitmap.getWidth(),jtTop + jtUpLeft.getHeight()+tipBitmap.getHeight());
                }
            }else if (screenW / 2 - 10 <= right - offestMargin - vWidth / 2 && right - offestMargin - vWidth / 2 <= screenW / 2 + 10){// center
                canvas.drawBitmap(jtUpCenter, left + offestMargin + vWidth / 2 - jtUpCenter.getWidth() / 2, jtTop, null);
                if(tipBitmap!=null){
                    int tipLeft = left + offestMargin + vWidth / 2 - tipBitmap.getWidth() / 2;
                    int tipTop = jtTop + jtUpCenter.getHeight();
                    canvas.drawBitmap(tipBitmap,tipLeft ,tipTop , null);
                    tipViewHitRect = new Rect(tipLeft,tipTop,tipLeft+tipBitmap.getWidth(),tipTop+tipBitmap.getHeight());
                }
            } else {
                canvas.drawBitmap(jtUpRight, left + vWidth / 2 - 100 - margin, jtTop, null);
                if (tipBitmap != null) {
                    int tipLeft = left + vWidth / 2 - 100 - tipBitmap.getWidth() / 2;
                    // 如果提示图片超出屏幕右边界
                    if (tipLeft + tipBitmap.getWidth() > screenW) {
                        tipLeft = screenW - tipBitmap.getWidth() - borderWitdh;
                    }
                    canvas.drawBitmap(tipBitmap, tipLeft, jtTop + jtUpRight.getHeight(), null);
                    tipViewHitRect = new Rect(tipLeft,jtTop + jtUpLeft.getHeight(),tipLeft+tipBitmap.getWidth(),jtTop + jtUpLeft.getHeight()+tipBitmap.getHeight());
                }
            }
        } else {// bottom
            int jtTop = getDownJTtop(jtDownLeft, top, vHeight);
            int jtDownCenterTop = getDownJTtop(jtDownCenter, top, vHeight);
            if (right < screenW / 2 || (screenW / 2 - left > right - screenW / 2)) {// 左
                canvas.drawBitmap(jtDownLeft, left + vWidth / 2, jtTop, null);
                if (tipBitmap != null) {
                    int tipLeft = left + vWidth / 2;
                    int tipTop = jtTop - tipBitmap.getHeight();
                    canvas.drawBitmap(tipBitmap, tipLeft, tipTop, null);
                    tipViewHitRect = new Rect(tipLeft,tipTop,tipLeft+tipBitmap.getWidth(),jtTop);
                }
            } else if (screenW / 2 - 10 <= right - offestMargin - vWidth / 2 && right - offestMargin - vWidth / 2 <= screenW / 2 + 10) {// 如果基本在中间(screenW/2-10<=target的中线<=screenW/2+10)
                canvas.drawBitmap(jtDownCenter, left + offestMargin + vWidth / 2 - jtDownCenter.getWidth() / 2, jtDownCenterTop, null);
                if (tipBitmap != null) {
                    int tipLeft = left + offestMargin + vWidth / 2 - tipBitmap.getWidth() / 2;
                    int tipTop = jtDownCenterTop - tipBitmap.getHeight();
                    canvas.drawBitmap(tipBitmap, tipLeft, tipTop, null);
                    tipViewHitRect = new Rect(tipLeft,tipTop,tipLeft+tipBitmap.getWidth(),jtDownCenterTop);
                }
            } else {// 右
                canvas.drawBitmap(jtDownRight, left + vWidth / 2 - 100 - margin, jtTop, null);
                if (tipBitmap != null) {
                    int tipLeft = left + vWidth / 2 - 100 - tipBitmap.getWidth() / 2 - margin;
                    // 如果提示图片超出屏幕右边界
                    if (tipLeft + tipBitmap.getWidth() > screenW) {
                        tipLeft = screenW - tipBitmap.getWidth() - borderWitdh;
                    }
                    canvas.drawBitmap(tipBitmap, tipLeft, jtTop - tipBitmap.getHeight(), null);
                    tipViewHitRect = new Rect(tipLeft,jtTop - tipBitmap.getHeight(),tipLeft+tipBitmap.getWidth(),jtTop);
                }
            }
        }


    }

    private int getUpJtTop(int targetBottom, int targetHeight) {
        int jtTop = 0;
        if (highLightStyle == VIEWSTYLE_CIRCLE) {
            jtTop = targetBottom + (radius - targetHeight / 2) + -margin + offestMargin;
        } else {
            jtTop = targetBottom + margin + offestMargin;
        }
        return jtTop;
    }

    private int getDownJTtop(Bitmap jtBitmap, int trgetTop, int targetHeight) {
        int jtTop = 0;
        if (highLightStyle == VIEWSTYLE_CIRCLE) {
            jtTop = trgetTop - (radius - targetHeight / 2) - jtBitmap.getHeight() - margin - offestMargin;
        } else {
            jtTop = trgetTop - jtBitmap.getHeight() - margin - offestMargin;
        }
        return jtTop;
    }

    /**
     * set the high light view
     *
     * @param targetView
     */
    public void setHighLightView(View targetView) {
        if (this.targetView != null && targetView != null && this.targetView != targetView) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mCanvas.drawPaint(paint);
            mCanvas.drawColor(maskColor);
        }
        this.targetView = targetView;
        invalidate();
        setVisibility(VISIBLE);
    }

    /**
     * set hightlight views .it will display them by order
     * @param targetView
     */
    public void setHightLightView(View... targetView){
        if(targetView!=null){
            targetViews = new ArrayList<View>();
            for(int i=0;i<targetView.length;i++){
                targetViews.add(i,targetView[i]);
            }
            setHighLightView(targetView[0]);
            targetViews.remove(0);
        }
    }

    /**
     * set the TouchOutside Dismiss listener
     *
     * @param cancel
     */
    public void setTouchOutsideDismiss(boolean cancel) {
        this.touchOutsideCancel = cancel;
    }

    /**
     * 设置额外的边框宽度
     *
     * @param borderWidth
     */
    public void setBorderWidth(int borderWidth) {
        this.borderWitdh = borderWidth;
    }

    /**
     * set the tip bitmap
     *
     * @param bitmap
     */
    public void setTipView(Bitmap bitmap) {
        this.tipBitmap = bitmap;
    }

    /**
     * set tip view resid
     * @param resId
     */
    public void setTipView(int resId){
        this.tipBitmap = BitmapFactory.decodeResource(getResources(), resId);
    }

    public void setTipView(View tipView, int width, int height) {
        Bitmap tipBitmap = MeasureUtil.drawViewToBitmap(tipView, width, height);
        if (null != tipBitmap) {
            setTipView(tipBitmap);
        }
    }

    /**
     * set cover mask color
     *
     * @param maskColor
     */
    public void setMaskColor(int maskColor) {
        this.maskColor = maskColor;
    }

    /**
     * @param statusBarHeight
     */
    public void setStatusBarHeight(int statusBarHeight) {
        this.statusBarHeight = statusBarHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP://
                if (touchOutsideCancel) {
                    if(targetViews==null||targetViews.size()==0){
                        this.setVisibility(View.GONE);
                        if (this.onDismissListener != null) {
                            onDismissListener.onDismiss(UserGuideView.this);
                        }
                    }else{
                        setHighLightView(targetViews.get(0));
                        targetViews.remove(0);
                    }
                    return true;
                }else{
                   int touchX = (int) event.getX();
                   int touchY = (int) event.getY();
                   if(tipViewHitRect!=null&&tipViewHitRect.contains(touchX,touchY)){
                       if(targetViews==null||targetViews.size()==0){
                           this.setVisibility(View.GONE);
                           if (this.onDismissListener != null) {
                               onDismissListener.onDismiss(UserGuideView.this);
                           }
                       }else{
                           setHighLightView(targetViews.get(0));
                           targetViews.remove(0);
                       }
                       return true;
                   }
                }
        }
        return true;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public int getOffestMargin() {
        return offestMargin;
    }

    /**
     * 光圈放大偏移值
     *
     * @param offestMargin
     */
    public void setOffestMargin(int offestMargin) {
        this.offestMargin = offestMargin;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.onDismissListener = listener;
    }

    public interface OnDismissListener {
        public void onDismiss(UserGuideView userGuideView);
    }
}
