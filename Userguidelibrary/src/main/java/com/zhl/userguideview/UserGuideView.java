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
import java.util.LinkedHashMap;
import java.util.Map;


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
    private ArrayList<Integer> tipViews;
    private Rect tipViewHitRect;
    private boolean showArrow = true;// 是否显示指示箭头
    private int jtUpLeftMoveX,jtUpRightMoveX,jtUpCenterMoveX, jtDownRightMoveX, jtDownLeftMoveX, jtDownCenterMoveX;
    private int tipViewMoveX;
    private LinkedHashMap<View,Integer> tipViewMoveXMap = new LinkedHashMap<>();
    private int tipViewMoveY;
    private LinkedHashMap<View,Integer> tipViewMoveYMap = new LinkedHashMap<>();

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
        try {//楼主之前的获取状态栏的高度为0，未验证所有设备，以下调整
            Resources resources = Resources.getSystem();
            statusBarHeight = resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", "android"));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
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
        // 绘制高亮框
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

        tipViewMoveX = getTipViewMoveX();
        tipViewMoveY = getTipViewMoveY();

        // 绘制箭头和提示view
        if (bottom < screenH / 2 || (screenH / 2 - top > bottom - screenH / 2)) {// top
            int jtTop = getUpJtTop(bottom, vHeight);
            if (right < screenW / 2 || (screenW / 2 - left > right - screenW / 2)) {//left
                if(showArrow){
                    canvas.drawBitmap(jtUpLeft, left + vWidth / 2+jtUpLeftMoveX, jtTop, null);
                }
                int tipTop = showArrow?jtTop + jtUpLeft.getHeight():jtTop;
                if (tipBitmap != null) {
                    canvas.drawBitmap(tipBitmap, left + vWidth / 2+tipViewMoveX, tipTop+tipViewMoveY, null);
                    tipViewHitRect = new Rect(left + vWidth / 2+tipViewMoveX,tipTop+tipViewMoveY,left + vWidth / 2+tipBitmap.getWidth(),tipTop+tipBitmap.getHeight());
                }
            }else if (screenW / 2 - 10 <= right - offestMargin - vWidth / 2 && right - offestMargin - vWidth / 2 <= screenW / 2 + 10){// center
                if(showArrow){
                    canvas.drawBitmap(jtUpCenter, left + offestMargin + vWidth / 2 - jtUpCenter.getWidth() / 2+jtUpCenterMoveX, jtTop, null);
                }
                if(tipBitmap!=null){
                    int tipLeft = left + offestMargin + vWidth / 2 - tipBitmap.getWidth() / 2;
                    int tipTop = showArrow?jtTop + jtUpCenter.getHeight():jtTop;
                    canvas.drawBitmap(tipBitmap,tipLeft+tipViewMoveX ,tipTop+tipViewMoveY , null);
                    tipViewHitRect = new Rect(tipLeft+tipViewMoveX,tipTop+tipViewMoveY,tipLeft+tipBitmap.getWidth(),tipTop+tipBitmap.getHeight());
                }
            } else {
                if(showArrow){
                    canvas.drawBitmap(jtUpRight, left + vWidth / 2 - 100 - margin+jtUpRightMoveX, jtTop, null);
                }
                if (tipBitmap != null) {
                    int tipLeft = left + vWidth / 2 - 100 - tipBitmap.getWidth() / 2;
                    int tipTop = showArrow?jtTop + jtUpRight.getHeight():jtTop;
                    // 如果提示图片超出屏幕右边界
                    if (tipLeft + tipBitmap.getWidth() > screenW) {
                        tipLeft = screenW - tipBitmap.getWidth() - borderWitdh;
                    }
                    canvas.drawBitmap(tipBitmap, tipLeft+tipViewMoveX, tipTop+tipViewMoveY, null);
                    tipViewHitRect = new Rect(tipLeft+tipViewMoveX,tipTop+tipViewMoveY,tipLeft+tipBitmap.getWidth(),tipTop+tipBitmap.getHeight());
                }
            }
        } else {// bottom
            int jtTop = getDownJTtop(jtDownLeft, top, vHeight);
            int jtDownCenterTop = getDownJTtop(jtDownCenter, top, vHeight);
            if (right < screenW / 2 || (screenW / 2 - left > right - screenW / 2)) {// 左
                if(showArrow){
                    canvas.drawBitmap(jtDownLeft, left + vWidth / 2+jtDownLeftMoveX, jtTop, null);
                }
                if (tipBitmap != null) {
                    int tipLeft = left + vWidth / 2;
                    int tipTop = showArrow?jtTop - tipBitmap.getHeight():top-tipBitmap.getHeight()-margin;
                    canvas.drawBitmap(tipBitmap, tipLeft+tipViewMoveX, tipTop+tipViewMoveY, null);
                    tipViewHitRect = new Rect(tipLeft+tipViewMoveX,tipTop+tipViewMoveY,tipLeft+tipBitmap.getWidth(),showArrow?jtTop:top);
                }
            } else if (screenW / 2 - 10 <= right - offestMargin - vWidth / 2 && right - offestMargin - vWidth / 2 <= screenW / 2 + 10) {// 如果基本在中间(screenW/2-10<=target的中线<=screenW/2+10)
                if(showArrow){
                    canvas.drawBitmap(jtDownCenter, left + offestMargin + vWidth / 2 - jtDownCenter.getWidth() / 2+jtDownCenterMoveX, jtDownCenterTop, null);
                }
                if (tipBitmap != null) {
                    int tipLeft = left + offestMargin + vWidth / 2 - tipBitmap.getWidth() / 2;
                    int tipTop = showArrow?jtDownCenterTop - tipBitmap.getHeight():top-tipBitmap.getHeight()-margin;
                    canvas.drawBitmap(tipBitmap, tipLeft+tipViewMoveX, tipTop+tipViewMoveY, null);
                    tipViewHitRect = new Rect(tipLeft+tipViewMoveX,tipTop+tipViewMoveY,tipLeft+tipBitmap.getWidth(),showArrow?jtDownCenterTop:top);
                }
            } else {// 右
                if(showArrow){
                    canvas.drawBitmap(jtDownRight, left + vWidth / 2 - 100 - margin+jtDownRightMoveX, jtTop, null);
                }
                if (tipBitmap != null) {
                    int tipLeft = left + vWidth / 2 - 100 - tipBitmap.getWidth() / 2 - margin;
                    // 如果提示图片超出屏幕右边界
                    if (tipLeft + tipBitmap.getWidth() > screenW) {
                        tipLeft = screenW - tipBitmap.getWidth() - borderWitdh;
                    }
                    int tipTop = showArrow?jtTop - tipBitmap.getHeight():top-tipBitmap.getHeight()-margin;
                    canvas.drawBitmap(tipBitmap, tipLeft+tipViewMoveX,tipTop+tipViewMoveY , null);
                    tipViewHitRect = new Rect(tipLeft+tipViewMoveX,tipTop+tipViewMoveY,tipLeft+tipBitmap.getWidth(),showArrow?jtTop:top);
                }
            }
        }

    }



    private int  getUpJtTop(int targetBottom, int targetHeight) {
        int jtTop = 0;
        if (highLightStyle == VIEWSTYLE_CIRCLE) {
            jtTop = targetBottom + (radius - targetHeight / 2) + margin + offestMargin;
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
     * set hightlight views .it will display them by order will use the same tipview
     * @param targetView
     */
    public void setHighLightView(View... targetView){
        if(targetView!=null){
            targetViews = new ArrayList<View>();
            for(int i=0;i<targetView.length;i++){
                targetViews.add(i,targetView[i]);
            }
            setHighLightView(targetView[0]);
            targetViews.remove(0);
        }
    }

    public void setHighLightView(LinkedHashMap<View,Integer> targetsWithTipViews){
        if(targetsWithTipViews!=null){
            targetViews = new ArrayList<View>();
            tipViews = new ArrayList<>();
            for(Map.Entry<View,Integer> entry:targetsWithTipViews.entrySet()){
                targetViews.add(entry.getKey());
                tipViews.add(entry.getValue());
            }
            tipBitmap = getBitmapFromResId(tipViews.get(0));
            tipViews.remove(0);
            setHighLightView(targetViews.get(0));
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
        this.tipBitmap = getBitmapFromResId(resId);
    }

    public Bitmap getBitmapFromResId(int resId){
        return BitmapFactory.decodeResource(getResources(), resId);
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
                        tipBitmap = getBitmapFromResId(tipViews.get(0));
                        tipViews.remove(0);
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
                           tipBitmap = getBitmapFromResId(tipViews.get(0));
                           tipViews.remove(0);
                           setHighLightView(targetViews.get(0));
                           targetViews.remove(0);
                       }
                       return true;
                   }
                }
        }
        return true;
    }

    /**
     * 箭头和tipview之间的margin
     * @return
     */
    public int getMargin() {
        return margin;
    }
    /**
     * 箭头和tipview之间的margin
     * @return
     */
    public void setMargin(int margin) {
        this.margin = margin;
    }

    public int getOffestMargin() {
        return offestMargin;
    }

    public boolean isShowArrow() {
        return showArrow;
    }

    public void setShowArrow(boolean showArrow) {
        this.showArrow = showArrow;
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

    public int getArrowUpLeftMoveX() {
        return jtUpLeftMoveX;
    }

    /**
     * 设置左上角箭头水平位移，用来做位置微调，为了不必须要的刷新重绘 限制在setHighlightView()方法前调用生效
     * @param jtUpLeftMoveX >0 向右偏移 <0向左偏移
     */
    public void setArrowUpLeftMoveX(int jtUpLeftMoveX) {
        this.jtUpLeftMoveX = jtUpLeftMoveX;
    }

    public int getArrowUpRightMoveX() {
        return jtUpRightMoveX;
    }

    /**
     * 设置右上角箭头水平位移，用来做位置微调，为了不必须要的刷新重绘 限制在setHighlightView()方法前调用生效
     * @param jtUpRightMoveX>0 向右偏移 <0向左偏移
     */
    public void setArrowUpRightMoveX(int jtUpRightMoveX) {
        this.jtUpRightMoveX = jtUpRightMoveX;
    }

    public int getArrowUpCenterMoveX() {
        return jtUpCenterMoveX;
    }

    /**
     * 设置上箭头水平位移，用来做位置微调，为了不必须要的刷新重绘 限制在setHighlightView()方法前调用生效
     * @param jtUpCenterMoveX>0 向右偏移 <0向左偏移
     */
    public void setArrowUpCenterMoveX(int jtUpCenterMoveX) {
        this.jtUpCenterMoveX = jtUpCenterMoveX;
    }

    public int getArrowDownRightMoveX() {
        return jtDownRightMoveX;
    }

    /**
     * 设置右下角箭头水平位移，用来做位置微调，为了不必须要的刷新重绘 限制在setHighlightView()方法前调用生效
     * @param jtDownRightMoveX>0 向右偏移 <0向左偏移
     */
    public void setArrowDownRightMoveX(int jtDownRightMoveX) {
        this.jtDownRightMoveX = jtDownRightMoveX;
    }

    public int getArrowDownLeftMoveX() {
        return jtDownLeftMoveX;
    }

    /**
     * 设置左下角箭头水平位移，用来做位置微调，为了不必须要的刷新重绘 限制在setHighlightView()方法前调用生效
     * @param jtDownLeftMoveX>0 向右偏移 <0向左偏移
     */
    public void setArrowDownLeftMoveX(int jtDownLeftMoveX) {
        this.jtDownLeftMoveX = jtDownLeftMoveX;
    }

    public int getArrowDownCenterMoveX() {
        return jtDownCenterMoveX;
    }

    /**
     * 设置下箭头水平位移，用来做位置微调，为了不必须要的刷新重绘 限制在setHighlightView()方法前调用生效
     * @param jtDownCenterMoveX>0 向右偏移 <0向左偏移
     */
    public void setArrowDownCenterMoveX(int jtDownCenterMoveX) {
        this.jtDownCenterMoveX = jtDownCenterMoveX;
    }

    /**
     * 设置tipview的水平位移 来微调位置 为了不必须要的刷新重绘 限制在setHighlightView()方法前调用生效
     * @param highlightView 与tipview对应的高亮view
     * @param tipViewMoveX >0 向右偏移 <0向左偏移
     */
    public void setTipViewMoveX(View highlightView,int tipViewMoveX){
        tipViewMoveXMap.put(highlightView,tipViewMoveX);
    }
    /**
     * 设置tipview的垂直位移 来微调位置 为了不必须要的刷新重绘 限制在setHighlightView()方法前调用生效
     * @param highlightView 与tipview对应的高亮view
     * @param tipViewMoveY >0 向右偏移 <0向左偏移
     */
    public void setTipViewMoveY(View highlightView,int tipViewMoveY){
        tipViewMoveYMap.put(highlightView,tipViewMoveY);
    }


    private int getTipViewMoveX() {
        Integer moveX = tipViewMoveXMap.get(targetView);
        return moveX==null?0:moveX;
    }
    private int getTipViewMoveY() {
        Integer moveY = tipViewMoveYMap.get(targetView);
        return moveY==null?0:moveY;
    }
}
