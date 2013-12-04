package com.yh.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.yh.web.view.MainActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

/**
 * 截屏
 * 
 * @author gudh
 * @data 2013-12-4
 */
public class ScreenShot {

	/**
	 * 截屏view
	 * 
	 * @param pActivity
	 * @return
	 */
	public static Bitmap drawShot(Activity pActivity) {
		View view = pActivity.getWindow().getDecorView();
		Bitmap bmp = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);
		view.draw(new Canvas(bmp));
		return bmp;
	}

	/**
	 * 进行截取屏幕,包含状态栏和标题栏
	 * 
	 * @param pActivity
	 * @return bitmap
	 */
	@SuppressWarnings("deprecation")
	public static Bitmap cacheShot(Activity pActivity) {
		Bitmap bitmap = null;

		View view = pActivity.getWindow().getDecorView();
		// 设置是否可以进行绘图缓存
		view.setDrawingCacheEnabled(true);
		// 如果绘图缓存无法，强制构建绘图缓存
		view.buildDrawingCache();
		// 返回这个缓存视图
		bitmap = view.getDrawingCache();

		// 获取状态栏高度
		Rect frame = new Rect();
		// 测量屏幕宽和高
		view.getWindowVisibleDisplayFrame(frame);
		int stautsHeight = frame.top;

		int width = pActivity.getWindowManager().getDefaultDisplay().getWidth();
		int height = pActivity.getWindowManager().getDefaultDisplay()
				.getHeight();

		// 根据坐标点和需要的宽和高创建bitmap
		bitmap = Bitmap.createBitmap(bitmap, 0, stautsHeight, width, height
				- stautsHeight);
		return bitmap;
	}

	/**
	 * 保存图片
	 * 
	 * @param pBitmap
	 */
	private static boolean savePic(Bitmap pBitmap, String strName) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(strName);
			if (null != fos) {
				pBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
				fos.flush();
				fos.close();
				return true;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 截图
	 * 
	 * @param activity
	 * @return 截图并且保存sdcard成功返回true，否则返回false
	 */
	public static boolean shotBitmap(Activity activity) {
		long s = System.currentTimeMillis();
		boolean bmp = savePic(drawShot(activity),
				"sdcard/yichaweb/" + System.currentTimeMillis() + ".png");
		long t = System.currentTimeMillis() - s;
		Log.i("ShotScreen", "use time:" + t);
		return bmp;
	}
	
	private static ScheduledFuture<?> x = null;
	private static Activity activity = null;
	/**
	 * 开始或者结束截图
	 * @param activity
	 * @param executor
	 */
	public static boolean startOrEndShot(final MainActivity activity,
			ScheduledExecutorService executor) {
		ScreenShot.activity = activity;
		if (x == null) {
			Runnable run = new Runnable() {
				@Override
				public void run() {
					// 使用Handler为了UI线程操作WebView
					activity.mHandler.sendEmptyMessage(MainActivity.SHOT);
				}
			};
			x = executor
					.scheduleWithFixedDelay(run, 0, 2000, TimeUnit.MILLISECONDS);
			return true;
		} else {
			x.cancel(false);
			x = null;
			return false;
		}
	}

	public static void shotOneBitmap() {
		if(activity != null){
			shotBitmap(activity);
		}
	}
}