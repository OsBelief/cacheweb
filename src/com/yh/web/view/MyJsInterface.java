package com.yh.web.view;

import android.app.Activity;
import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * JS条用Java接口
 * @author gudh
 * @date 2013-12-09
 *
 */
public class MyJsInterface {
	
	private Activity activity;
	
	public MyJsInterface(){
	}
	
	public void setSecondActivity(Activity activity){
		this.activity = activity;
	}
	
	@JavascriptInterface
	public void historyGo(int i){
		Log.d("JSInterface", "historyGo:" + i);
		if(activity != null){
			activity.finish();
			activity = null;
		}
	}

}
