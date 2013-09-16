package com.yh.web.view;

import com.yh.web.R;

import android.app.Activity;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

/**
 * @author gudh �Զ��������WebChromeClient
 */
public class MyWebChromeClient extends WebChromeClient {

	private Activity act;

	public MyWebChromeClient(Activity act) {
		this.act = act;
	}

	/**
	 * ������
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

}
