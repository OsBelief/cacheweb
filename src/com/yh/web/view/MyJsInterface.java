package com.yh.web.view;

import android.os.Message;
import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * JS条用Java接口
 * @author gudh
 * @date 2013-12-09
 *
 */
public class MyJsInterface {
	
	private MainActivity factivity;
	private MainActivity sactivity;
	
	public MyJsInterface(){
	}
	
	public void setFirstActivity(MainActivity activity){
		this.factivity = activity;
	}
	
	public void setSecondActivity(MainActivity activity){
		this.sactivity = activity;
	}
	
	@JavascriptInterface
	public void historyGo(int i){
		Log.d("JSInterface", "historyGo:" + i);
		if(sactivity != null){
			if(MainActivity.canFinish){
				sactivity.finish();
				sactivity = null;
			} else{
				// 切换为主Activity
				factivity = sactivity;
				sactivity = null;
				// 先修改tUrl再load
				factivity.tUrl = MainActivity.DEFAULT_URL;
				factivity.web.loadUrl(MainActivity.DEFAULT_URL);
			}
		} else if(factivity != null){
			Message msg = new Message();
			msg.what = MainActivity.HISTORY_GO;
			msg.arg1 = i;
			factivity.mHandler.sendMessageDelayed(msg , 1);
		}
	}

}
