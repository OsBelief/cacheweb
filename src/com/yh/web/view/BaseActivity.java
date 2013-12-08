package com.yh.web.view;

import java.util.LinkedList;

import android.app.Activity;
import android.os.Bundle;

/**
 * @author gudh 自定义Activity的基类
 */
public class BaseActivity extends Activity {

	public static LinkedList<Activity> sAllActivitys = new LinkedList<Activity>();

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		sAllActivitys.add(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sAllActivitys.remove(this);
	}

	public static void finishAll() {
		for (Activity activity : sAllActivitys) {
			activity.finish();
		}
		sAllActivitys.clear();
	}

	/**
	 * 这个主要是用来关闭进程的
	 */
	public static void exit() {
		finishAll();
		// 光把所有activity finish 的话，进程是不会关闭的
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}
}
