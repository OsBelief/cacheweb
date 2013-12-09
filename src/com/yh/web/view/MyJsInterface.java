package com.yh.web.view;

import cn.yicha.cache.fuli.R;
import android.app.Activity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

/**
 * JS条用Java接口
 * @author gudh
 * @date 2013-12-09
 *
 */
public class MyJsInterface {
	
	private Activity factivity;
	private Activity sactivity;
	
	public MyJsInterface(){
	}
	
	public void setFirstActivity(Activity activity){
		this.factivity = activity;
	}
	
	public void setSecondActivity(Activity activity){
		this.sactivity = activity;
	}
	
	@JavascriptInterface
	public void historyGo(int i){
		Log.d("JSInterface", "historyGo:" + i);
		if(sactivity != null){
			sactivity.finish();
			sactivity = null;
		} else if(factivity != null){
			WebView web = (WebView) factivity.findViewById(R.id.webView1);
			if(web.canGoBack()){
				web.goBackOrForward(i);
			}
		}
	}

}
