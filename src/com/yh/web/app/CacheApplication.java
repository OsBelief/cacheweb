package com.yh.web.app;

import android.app.Application;

/**
 * 全局应用，捕获异常
 * 
 * @author gudh
 * @data 2013-12-10
 */
public class CacheApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		GlobalException customException = GlobalException.getInstance();
		customException.init(this.getApplicationContext());
	}
}
