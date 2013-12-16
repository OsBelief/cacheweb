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
	private final static long LOAD_TIME = 15000;
	// 进度超过80%直接置为100
	private final static long PROCESS_MAX = 80;
	
	// 显示进度的Activity
	private MainActivity act;
	
	// 上次进度改变时间，为0表示没有url在加载
	public volatile static long lastUpdateTime = 0;
	public static MainActivity nowAct = null;
	
	static{
		// 开启线程检测连接问题
				new Thread(new Runnable() {
					@Override
					public void run() {
						while(true){
							if(nowAct != null){
								checkProcess(nowAct);
							}
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}).start();
	}

	public MyWebChromeClient(MainActivity act) {
		this.act = act;
	}
	
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
		
		if(newProgress >= PROCESS_MAX){
			lastUpdateTime = 0;
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
	 * 设置当前要监控进度的activity
	 * @param act
	 * @return
	 */
	public static void setRefreshActivity(MainActivity act){
		nowAct = act;
		lastUpdateTime = 0;
	}
	/**
	 * 检测加载情况，判断是否显示提示
	 */
	private static void checkProcess(MainActivity act){
		if(lastUpdateTime == 0){
			return;
		}
		long time = System.currentTimeMillis() - lastUpdateTime;
		if(time > LOAD_TIME){
			Log.d("CheckProcess", "show dialog, time:" + time);
			showRefreshDialog(act);
		} else{
			Log.d("CheckProcess", "time not exceed, time:" + time);
		}
	}
	// 标志同一时刻仅有一个显示对话框
	private volatile static boolean isRefresing = false;
	/**
	 * 显示刷新提示
	 */
	protected static void showRefreshDialog(final MainActivity act) {
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
