package com.zhl.userguideview;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View.MeasureSpec;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

/**
 * 测绘工具类
 * @since 2014/11/19
 */
public final class MeasureUtil {
	public static final int RATION_WIDTH = 0;
	public static final int RATION_HEIGHT = 1;
	
	/**
	 * 获取屏幕尺寸(miui系统在没有底部导航栏的情况下获取的高度仍然是顶部到导航栏的高度所以要加上导航栏高度)
	 * 
	 * @param context
	 * @return 屏幕尺寸像素值，下标为0的值为宽，下标为1的值为高
	 */
	public static int[] getScreenSize(Context context) {
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getRealMetrics(metrics);
		return new int[] { metrics.widthPixels, metrics.heightPixels};
	}

	/**
	 * 是miui系统才加上底部导航栏高度
	 */
	private static int getBottomBarHeight(Context context) {
		int bottomHeight = 0;
		if ("Xiaomi".equalsIgnoreCase(Build.MANUFACTURER)) {
			//如果虚拟按键已经显示，则不需要补充高度,否则补充高度
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
					&& Settings.Global.getInt(context.getContentResolver(), "force_fsg_nav_bar", 0) != 0) {
				bottomHeight = getNavgitarBarHeight(context);
				return bottomHeight;
			}
		}

		return bottomHeight;
	}

	/**
	 * 获取底部导航栏高度
	 *
	 * @param context
	 * @return
	 */
	public static int getNavgitarBarHeight(Context context) {
		int navigatorBarHeight = 0;
		int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
		if (resourceId > 0) {
			navigatorBarHeight = context.getResources().getDimensionPixelSize(resourceId);
		}
		return navigatorBarHeight;
	}

	/**
	 * 自定义控件获取测量后的尺寸方法
	 * 
	 * @param measureSpec
	 *            测量规格
	 * @param ratio
	 *            宽高标识
	 * @param mStr 
	 * 			  自定义控件上需要测绘的文字
	 * @param mBitmap
	 * 			  自定义控件上需要测绘的图片
	 * @param paddings
	 * 			 自定义控件的padding值int[]{left,top,right,bottom}
	 * @return 宽或高的测量值
	 */
	public static int getMeasureSize(int measureSpec, int ratio,String mStr,Bitmap mBitmap,Paint mPaint,int[] paddings) {
		// 声明临时变量保存测量值
		int result = 0;
		/*
		 * 获取测量mode和size
		 */
		int mode = MeasureSpec.getMode(measureSpec);
		int size = MeasureSpec.getSize(measureSpec);
		/*
		 * 判断mode的具体值
		 */
		switch (mode) {
		case MeasureSpec.EXACTLY:// EXACTLY时直接赋值
			result = size;
			break;
		default:// 默认情况下将UNSPECIFIED和AT_MOST一并处理
			if (ratio == RATION_WIDTH) {
				if(mStr!=null&&mBitmap!=null){
					float textWidth = mPaint.measureText(mStr);
					result = ((int)(textWidth >= mBitmap.getWidth() ? textWidth : mBitmap.getWidth())) ;
				}else if(mBitmap!=null){
					result = mBitmap.getWidth();
				}else if(mStr!=null){
					result = (int) mPaint.measureText(mStr);
				}
				if(paddings!=null){
					result+=( paddings[0] + paddings[2]);
				}
			} else if (ratio == RATION_HEIGHT) {
				if(mStr!=null&&mBitmap!=null){
					result = ((int) ((mPaint.descent() - mPaint.ascent()) * 2 + mBitmap.getHeight()));
				}else if(mBitmap!=null){
					result = mBitmap.getHeight();
				}else if(mStr!=null){
					result = (int) ((mPaint.descent() - mPaint.ascent()) * 2);
				}
				if(paddings!=null){
					result+=( paddings[1] + paddings[3]);
				}
			}

			/*
			 * AT_MOST时判断size和result的大小取小值
			 */
			if (mode == MeasureSpec.AT_MOST) {
				result = Math.min(result, size);
			}
			break;
		}
		return result;
	}
	public static Bitmap changeColor(Bitmap src, int keyColor, int replColor, int tolerance) {
		Bitmap copy = src.copy(Bitmap.Config.ARGB_8888, true);
		int width = copy.getWidth();
		int height = copy.getHeight();
		int[] pixels = new int[width * height];
		src.getPixels(pixels, 0, width, 0, 0, width, height);
		int sR = Color.red(keyColor);
		int sG = Color.green(keyColor);
		int sB = Color.blue(keyColor);
		int tR = Color.red(replColor);
		int tG = Color.green(replColor);
		int tB = Color.blue(replColor);
		float[] hsv = new float[3];
		Color.RGBToHSV(tR, tG, tB, hsv);
		float targetHue = hsv[0];
		float targetSat = hsv[1];
		float targetVal = hsv[2];

		for (int i = 0; i < pixels.length; ++i) {
			int pixel = pixels[i];

			if (pixel == keyColor) {
				pixels[i] = replColor;
			} else {
				int pR = Color.red(pixel);
				int pG = Color.green(pixel);
				int pB = Color.blue(pixel);

				int deltaR = Math.abs(pR - sR);
				int deltaG = Math.abs(pG - sG);
				int deltaB = Math.abs(pB - sB);

				if (deltaR <= tolerance && deltaG <= tolerance && deltaB <= tolerance) {
					Color.RGBToHSV(pR, pG, pB, hsv);
					hsv[0] = targetHue;
					hsv[1] = targetSat;
					hsv[2] *= targetVal;

					int mixTrgColor = Color.HSVToColor(Color.alpha(pixel), hsv);
					pixels[i] = mixTrgColor;
				}
			}
		}

		copy.setPixels(pixels, 0, width, 0, 0, width, height);

		return copy;
	}

	public static int dp2px(Context context, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}
	public static float dp2px(Context context, float dp) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}
	public static int sp2px(Context context,int sp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
	}
	public static int px2dp(Context context,int px) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context.getResources().getDisplayMetrics());
	}

	/**
	 * 测量文字的高度
	 * @param textsize px
	 * @return
     */
	public static int measureTextHeight(int textsize){
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextSize(textsize);
		Paint.FontMetrics metrics = paint.getFontMetrics();
		return (int) Math.ceil(metrics.descent-metrics.ascent);
	}

	/**
	 * 测量文字的宽度
	 * @param text
	 * @param textsize px
     * @return
     */
	public static int measureTextWidth(String text,int textsize){
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextSize(textsize);
		return (int) paint.measureText(text);
	}

	public static Bitmap createBitmapSafely(int width, int height, Bitmap.Config config, int retryCount) {
		try {
			return Bitmap.createBitmap(width, height, config);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			if (retryCount > 0) {
				System.gc();
				return createBitmapSafely(width, height, config, retryCount - 1);
			}
			return null;
		}
	}

	public static int getScreenDPI(Context context){
		if(context==null){
			return 0;
		}
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics.densityDpi;
	}

	public static float getScreenDensity(Context context){
		if(context==null){
			return 0;
		}
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics.density;
	}

	/**
	 * 获取屏幕高宽比
	 * @param context
	 * @return
	 */
	public static float getScreenRatio(Context context) {
		int size[] = getScreenSize(context);
		return (float) size[1]/(float)size[0];
	}

	/**
	 * 获取状态栏高度
	 * @param context
	 * @return
	 */
	public static int getStatuBarHeight(Context context){
//		Rect frame = new Rect();
//		((Activity)context).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
//		if (Build.VERSION.SDK_INT < 19) {
//			frame.top = 44;
//		}
//		return frame.top;
//		int result = 0;
//		// 适配Android 11的状态栏高度选择
//		if(Build.VERSION.SDK_INT>=30){
////			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
////			WindowMetrics windowMetrics = wm.getCurrentWindowMetrics();
////			WindowInsets windowInsets = windowMetrics.getWindowInsets();
////			Insets insets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars() | WindowInsets.Type.displayCutout());
////			result = insets.top;
//		}else{
//			int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
//			if (resourceId > 0) {
//				result = context.getResources().getDimensionPixelSize(resourceId);
//			}
//		}
//		return result;

		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}


	/**
	 * 判断虚拟导航栏是否显示
	 * @param window
	 * @return true(显示虚拟导航栏)，false(不显示或不支持虚拟导航栏)
	 */
	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static boolean checkNavigationBarShow(@androidx.annotation.NonNull Context context, @androidx.annotation.NonNull Window window) {
		boolean show;
		Display display = window.getWindowManager().getDefaultDisplay();
		Point point = new Point();
		display.getRealSize(point);

		android.view.View decorView = window.getDecorView();
		Configuration conf = context.getResources().getConfiguration();
		if (Configuration.ORIENTATION_LANDSCAPE == conf.orientation) {
			android.view.View contentView = decorView.findViewById(android.R.id.content);
			show = (point.x != contentView.getWidth());
		} else {
			Rect rect = new Rect();
			decorView.getWindowVisibleDisplayFrame(rect);
			show = (rect.bottom != point.y);
		}
		return show;
	}

	/**
	 * 获取导航栏高度
	 * @param context
	 * @return
	 */
	public static int getNavigationHeight(Context context) {
		int resourceId = 0;
		int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
		if (rid != 0) {
			resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
			return context.getResources().getDimensionPixelSize(resourceId);
		} else{
			return 0;
		}
	}

	public static int measureTextLines(Context context, android.widget.TextView tv, String str) {
		if (tv == null || TextUtils.isEmpty(str)) {
			return 0;
		}
		Paint paint = new Paint();
		paint.setTextSize(tv.getTextSize());
		float textWidth = paint.measureText(str);
		return (int) Math.ceil(textWidth / (MeasureUtil.getScreenSize(context)[0] - MeasureUtil.dp2px(context,124)));
	}

}
