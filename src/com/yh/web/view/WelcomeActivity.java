package com.yh.web.view;

import java.lang.reflect.Field;

import com.yh.web.cache.NetMonitor;

import cn.yicha.cache.fuli.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;

/**
 * 启动时Activity
 * 
 * @author gudh
 * @data 2013-11-29
 */
public class WelcomeActivity extends BaseActivity {
	private static final int GO_HOME = 1000;
	private static final int GO_GUIDE = 1001;
	
	// 延迟1秒
	private static final long DELAY_MILLIS = 1000;

	public static final String FIRSTSTART_PREF = "first_start";
	public static final String FIRSTSTART_KEY = "is_first";

	/**
	 * Handler:跳转到不同界面
	 */
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GO_HOME:
				goActivity(MainActivity.class);
				break;
			case GO_GUIDE:
				goActivity(GuideActivity.class);
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);

		init();
	}

	/**
	 * 判断网络是否可用，并进入主界面
	 */
	public void init(){
		if(NetMonitor.isNetworkAvailable(this)){
			initEntry();
		}else{
			showNetUnavailable();
		}
	}
	
	/**
	 * 进入
	 */
	private void initEntry() {
		// 判断是否是第一次运行
		SharedPreferences preferences = getSharedPreferences(
				FIRSTSTART_PREF, MODE_PRIVATE);
		boolean isFirstIn = preferences.getBoolean(FIRSTSTART_KEY, true);

		if (!isFirstIn) {
			// 使用Handler的postDelayed方法，1秒后执行跳转到MainActivity
			mHandler.sendEmptyMessageDelayed(GO_HOME, DELAY_MILLIS);
		} else {
			mHandler.sendEmptyMessageDelayed(GO_GUIDE, DELAY_MILLIS);
		}
	}
	
	/**
	 * 显示网络不可用
	 */
	private void showNetUnavailable() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("当前网络不可用，易查福利需要在有网络的情况下访问，请检查网络重试或退出。")
				.setTitle("网络连接提示")
				.setPositiveButton("重试", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
		                    field.setAccessible(true);   
		                    field.set(dialog, false);
						} catch (NoSuchFieldException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}   
						init();
						
					}
				})
				.setNegativeButton("退出", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						BaseActivity.exit();
					}
				}).setCancelable(false).create().show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.welcome, menu);
		return true;
	}

	private void goActivity(Class<?> cls) {
		Intent intent = new Intent(WelcomeActivity.this, cls);
		WelcomeActivity.this.startActivity(intent);
		WelcomeActivity.this.finish();
	}
}
