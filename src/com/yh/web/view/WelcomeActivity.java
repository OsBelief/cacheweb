package com.yh.web.view;

import cn.yicha.cache.fuli.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
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

	private void init() {
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
