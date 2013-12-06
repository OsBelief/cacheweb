package com.yh.web.view;

import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import cn.yicha.cache.fuli.R;

/**
 * @author gudh 自定义浏览器WebChromeClient
 */
public class MyWebChromeClient extends WebChromeClient {

	private MainActivity act;

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
}
