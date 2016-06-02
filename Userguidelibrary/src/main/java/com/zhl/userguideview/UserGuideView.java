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



/**
 * 描述：一个用于“应用新特性”的用户指引view
 * Created by zhaohl on 2015-11-26.
 */
public class UserGuideView extends View {
    public static final int VIEWSTYLE_RECT=0;
    public static final int VIEWSTYLE_CIRCLE=1;
    public static final int VIEWSTYLE_OVAL=2;
    public static final int MASKBLURSTYLE_SOLID=0;
    public static final int MASKBLURSTYLE_NORMAL=1;
    private Bitmap fgBitmap;
    private Bitmap jtUpLeft,jtUpRight,jtDownRight,jtDownLeft;
    private Canvas mCanvas;
    private Paint mPaint;
    private int screenW, screenH;
    private View targetView;
    private boolean touchOutsideCancel = true;
    private int borderOffset=20;
    private int margin=40;
    private int highLightStyle = VIEWSTYLE_RECT;
    public int maskblurstyle = MASKBLURSTYLE_SOLID;
    private Bitmap tipBitmap;
    private int radius;
    private int maskColor = 0x99000000;
    private OnDismissListener onDismissListener;
    private int statusBarHeight = 0;

    public UserGuideView(Context context){
        this(context,null);
    }
    public UserGuideView(Context context, AttributeSet set) {
        this(context, set, -1);
    }

    public UserGuideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
        if(attrs!=null){
            TypedArray array =  context.obtainStyledAttributes(attrs, R.styleable.UserGuideView);
            highLightStyle = array.getInt(R.styleable.UserGuideView_HighlightViewStyle, VIEWSTYLE_RECT);
            maskblurstyle = array.getInt(R.styleable.UserGuideView_MaskBlurStyle, MASKBLURSTYLE_SOLID);
            BitmapDrawable drawable = (BitmapDrawable) array.getDrawable(R.styleable.UserGuideView_tipView);
            maskColor = array.getColor(R.styleable.UserGuideView_maskColor,maskColor);
            if(drawable!=null){
                tipBitmap = drawable.getBitmap();
            }
            array.recycle();
        }
        cal(context);

        init(context);
    }

    private void cal(Context context) {
        int[] screenSize = MeasureUtil.getScreenSize((Activity) context);

        screenW = screenSize[0];
        screenH = screenSize[1];
        Rect frame = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        statusBarHeight = frame.top;
		 if(Build.VERSION.SDK_INT< Build.VERSION_CODES.KITKAT){
        	statusBarHeight=44;
		}
    }

    private void init(Context context) {

//        setLayerType(LAYER_TYPE_SOFTWARE,null);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        mPaint.setARGB(0, 255, 0, 0);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        BlurMaskFilter.Blur blurStyle  = null;
        switch (maskblurstyle){
            case MASKBLURSTYLE_SOLID:
                blurStyle = BlurMaskFilter.Blur.SOLID;
                break;
            case MASKBLURSTYLE_NORMAL:
                blurStyle = BlurMaskFilter.Blur.NORMAL;
                break;
        }

        mPaint.setMaskFilter(new BlurMaskFilter(15, blurStyle));

        fgBitmap = MeasureUtil.createBitmapSafely(screenW, screenH, Bitmap.Config.ARGB_8888,2);
        if(fgBitmap==null){
            throw new RuntimeException("out of memery cause fgbitmap create fail");
        }
        mCanvas = new Canvas(fgBitmap);

        mCanvas.drawColor(maskColor);

        jtDownRight = BitmapFactory.decodeResource(getResources(), R.drawable.jt_down_right);
        jtDownLeft = BitmapFactory.decodeResource(getResources(), R.drawable.jt_down_left);
        jtUpLeft = BitmapFactory.decodeResource(getResources(), R.drawable.jt_up_left);
        jtUpRight = BitmapFactory.decodeResource(getResources(), R.drawable.jt_up_right);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(targetView==null){
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

//        ViewGroup decorView = null;
//        Context context = this.getContext();
//        if(context instanceof Activity){
//            decorView = (ViewGroup) ((Activity) context).getWindow().getDecorView();
//        }
//        View temp = targetView;
//        while(temp!=decorView){
//            temp = (View) temp.getParent();
//            if(temp==null){
//                break;
//            }
//            left+=temp.getLeft();
//            top+=temp.getTop();
//        }
//        top -= statusBarHeight;
//        right = left+vWidth;
//        bottom = top+vHeight;


        Rect tagetRect = new Rect();
        targetView.getGlobalVisibleRect(tagetRect);
        tagetRect.offset(0, -statusBarHeight);
        left = tagetRect.left;
        top = tagetRect.top;
        right = tagetRect.right;
        bottom = tagetRect.bottom;

        if(left==0){
            left+=borderOffset;
        }else if(top==0){
            top+=borderOffset;
        }else if(right==screenW){
            right-=borderOffset;
        }else if(bottom==screenH){
            bottom-=borderOffset;
        }
        switch (highLightStyle){
            case VIEWSTYLE_RECT:
                RectF rect = new RectF(left,top,right,bottom);
                mCanvas.drawRoundRect(rect, 20, 20, mPaint);
                break;
            case VIEWSTYLE_CIRCLE:
                radius = vWidth < vHeight ? vWidth / 2 +2*borderOffset: vHeight / 2+2*borderOffset;
                if(radius<50){
                    radius = 100;
                }
                mCanvas.drawCircle(left+vWidth / 2, top+vHeight / 2,radius, mPaint);
                break;
            case VIEWSTYLE_OVAL:
                RectF rectf = new RectF(left,top,right,bottom);
                mCanvas.drawOval(rectf, mPaint);
                break;

        }

        if(bottom<screenH/2||(screenH/2-top>bottom-screenH/2)){// top
            int jtTop = highLightStyle==VIEWSTYLE_CIRCLE?bottom+radius-margin:bottom+margin;
            if(right<screenW/2||(screenW/2-left>right-screenW/2)){//left
                canvas.drawBitmap(jtUpLeft,left+vWidth/2,jtTop,null);
                if(tipBitmap!=null){
                    canvas.drawBitmap(tipBitmap,left+vWidth/2,jtTop+jtUpLeft.getHeight(),null);
                }
            }else{
                canvas.drawBitmap(jtUpRight,left+vWidth/2-100-margin,jtTop,null);
                if(tipBitmap!=null){
                    canvas.drawBitmap(tipBitmap,left+vWidth/2-100-tipBitmap.getWidth()/2,jtTop+jtUpRight.getHeight(),null);
                }
            }
        }else{
            int jtTop = highLightStyle==VIEWSTYLE_CIRCLE?top-radius-margin:top - jtDownLeft.getHeight()-margin;
            if(right<screenW/2||(screenW/2-left>right-screenW/2)){
                canvas.drawBitmap(jtDownLeft, left+vWidth / 2, jtTop,null);
                if(tipBitmap!=null){
                    canvas.drawBitmap(tipBitmap,left+vWidth/2,jtTop-tipBitmap.getHeight(),null);
                }
            }else{
                canvas.drawBitmap(jtDownRight, left+vWidth / 2-100-margin, jtTop,null);
                if(tipBitmap!=null){
                    canvas.drawBitmap(tipBitmap,left+vWidth/2-100-tipBitmap.getWidth()/2-margin,jtTop-tipBitmap.getHeight(),null);
                }
            }
        }


    }

    /**
     * set the high light view
     * @param targetView
     */
    public void setHighLightView(View targetView){
            this.targetView = targetView;
            invalidate();
    }

    /**
     * set the TouchOutside Dismiss listener
     * @param cancel
     */
    public void setTouchOutsideDismiss(boolean cancel){
        this.touchOutsideCancel = cancel;
    }

	/**
     * set offset
     * @param borderOffset
     */
    public void setBorderOffset(int borderOffset){
    	this.borderOffset = borderOffset;
    }
    /**
     * set the tip bitmap
     * @param bitmap
     */
    public void setTipView(Bitmap bitmap){
        this.tipBitmap = bitmap;
    }

    /**
     * set cover mask color
     * @param maskColor
     */
    public void setMaskColor(int maskColor){
        this.maskColor = maskColor;
    }

    /**
     * @param statusBarHeight
     */
    public void setStatusBarHeight(int statusBarHeight){
        this.statusBarHeight = statusBarHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP://
                if(touchOutsideCancel){
                    this.setVisibility(View.GONE);
                    if(this.onDismissListener!=null){
                        onDismissListener.onDismiss();
                    }
                    return true;
                }
                break;
        }
        return true;
    }

    public void setOnDismissListener(OnDismissListener listener){
        this.onDismissListener = listener;
    }

    public interface OnDismissListener{
        public void onDismiss();
    }
}
