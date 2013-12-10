package com.yh.web.app;

import java.lang.Thread.UncaughtExceptionHandler;

import cn.yicha.cache.fuli.R;

import com.yh.web.view.BaseActivity;
import com.yh.web.view.MainActivity;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;

/**
 * 全局异常捕获
 * 
 * @author gudh
 * @data 2013-12-10
 */
public class GlobalException implements UncaughtExceptionHandler {
	
	// 获取application 对象；
	private Context context;

	private Thread.UncaughtExceptionHandler defaultExceptionHandler;
	
	// 单例声明CustomException;
	private static GlobalException customException;

	public static GlobalException getInstance() {
		if (customException == null) {
			customException = new GlobalException();
		}
		return customException;
	}
	
	public void init(Context context) {
		this.context = context;
		defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable exception) {
		if (defaultExceptionHandler != null) {
			Log.e("Error", context.getPackageName() + " exception > " +  exception.getLocalizedMessage());
			// 将异常抛出，则应用会弹出异常对话框.这里先注释掉
			// defaultExceptionHandler.uncaughtException(thread, exception);
			
			// 显示退出提示框
			showRestartDialog(MainActivity.nowMainAct, exception.getMessage());
		}
	}
	
	public static void showRestartDialog(final MainActivity context, String error){
		if(context == null){
			BaseActivity.exit();
			return;
		}
		Builder builder = new Builder(context);
		builder.setMessage("对不起，程序出现异常，需要重启。\n" + error);
		builder.setTitle("提示");
		builder.setPositiveButton("重启", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				context.exitAndStartNew(context.getString(R.string.defaultUrl));
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("退出", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				BaseActivity.exit();
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
}
