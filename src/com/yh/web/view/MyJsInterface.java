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
			sactivity.finish();
			sactivity = null;
		} else if(factivity != null){
			Message msg = new Message();
			msg.what = MainActivity.HISTORY_GO;
			msg.arg1 = i;
			factivity.mHandler.sendMessageDelayed(msg , 1);
		}
	}

}
