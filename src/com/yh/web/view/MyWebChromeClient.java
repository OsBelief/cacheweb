package com.yh.web.view;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Message;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import cn.yicha.cache.fuli.R;

/**
 * @author gudh 自定义浏览器WebChromeClient
 */
public class MyWebChromeClient extends WebChromeClient {
	// 判断超时时间，用于提示刷新
	private final static long LOAD_TIME = 8000;
	
	private MainActivity act;

	public MyWebChromeClient(MainActivity act) {
		this.act = act;
		
		// 开启线程检测连接问题
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					checkProcess();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	// 上次进度改变时间，为0表示没有url在加载
	private volatile long lastUpdateTime = 0;
	
	/**
	 * 进度条
	 * 
	 * @param view
	 * @param newProgress
	 */
	@Override
	public void onProgressChanged(WebView view, int newProgress) {
		// 加载超时显示提示
		if(newProgress == 100){
			lastUpdateTime = 0;
		} else if(lastUpdateTime == 0){
			lastUpdateTime = System.currentTimeMillis();
		}
		
		if(newProgress >= 80){
			Log.d("Process", "Update Process:" + newProgress);
			newProgress = 100;
		}
		ProgressBar progressBar = (ProgressBar) act
				.findViewById(R.id.progressBar);
		progressBar.setProgress(newProgress);
	}
	
	// For Android < 3.0
	public void openFileChooser(ValueCallback<Uri> uploadMsg) {
		act.mUploadMessage = uploadMsg;
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType("image/*");
		act.startActivityForResult(Intent.createChooser(i, "File Chooser"),
				MainActivity.FILECHOOSER_RESULTCODE);
	}
	
	// For Android 3.0+
	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
		openFileChooser(uploadMsg);
	}
	
	// android 4.1
	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
		openFileChooser(uploadMsg);
	}
	
	/**
	 * 检测加载情况，判断是否显示提示
	 */
	private void checkProcess(){
		if(lastUpdateTime == 0){
			Log.d("CheckProcess", "check once: no onLoad");
			return;
		}
		long time = System.currentTimeMillis() - lastUpdateTime;
		if(time > LOAD_TIME){
			Log.d("CheckProcess", "show dialog, time:" + time);
			showRefreshDialog();
		} else{
			Log.d("CheckProcess", "time not exceed, time:" + time);
		}
	}
	// 标志同一时刻仅有一个显示对话框
	private volatile boolean isRefresing = false;
	/**
	 * 显示刷新提示
	 */
	protected void showRefreshDialog() {
		if(isRefresing){
			return;
		}
		isRefresing = true;
		Builder builder = new Builder(act);
		builder.setMessage("网络不太好？可以尝试刷新一下。");
		builder.setTitle("易查福利");
		builder.setPositiveButton("刷新", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				lastUpdateTime = 0;
				act.web.reload();
				isRefresing = false;
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				lastUpdateTime = 0;
				isRefresing = false;
				dialog.dismiss();
			}
		});
		builder.setIcon(R.drawable.confirm).setCancelable(false);
		
		Message msg = new Message();
		msg.what = MainActivity.SHOW_DIALGO;
		msg.obj = builder;
		act.mHandler.sendMessage(msg);
	}
}
