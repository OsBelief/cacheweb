package com.yh.web.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import cn.yicha.cache.fuli.R;
import com.yh.web.cache.CacheControl;
import com.yh.web.cache.HttpUtil;
import com.yh.web.cache.IOUtil;

/**
 * @author gudh 自定义浏览器WebViewClient
 */
public class MyWebViewClient extends WebViewClient {

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	private Activity act;

	private String errorHtm;
	
	// 记录302跳转情况
	private String pendingUrl;

	public MyWebViewClient(Activity act) {
		this.act = act;
		try {
			errorHtm = IOUtil.readStream(act.getAssets().open("error.htm"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		Log.i("shouldOverrideUrlLoading", url);
		((EditText) act.findViewById(R.id.uText)).setText(url);
		String reload = HttpUtil.getToUrl(url);
		if (reload != null) {
			url = reload;
		}
		view.loadUrl(url);
		checkFuliLoopLinks(view, url);
		return false;
	}
	
	private List<String> lastUrls = new ArrayList<String>(4);
	/**
	 * 检测福利循环链接
	 * @param view
	 * @param url
	 */
	public void checkFuliLoopLinks(WebView view, String url){
		if(lastUrls.size() < 2){
			lastUrls.add(url);
		} else {
			// 判断当前URL和最近第二个都是福利并且相同，前一个是password域名下的，则跳到默认主页
			if(url.contains("fuli.yicha.cn") && lastUrls.get(lastUrls.size() - 2).endsWith(url)){
				if(lastUrls.get(lastUrls.size() - 1).contains("passport.yicha.cn")){
					Log.i("302Back", "Go to main, Now:" + url);
					//view.goBackOrForward(-3);
					url = act.getString(R.string.defaultUrl);
					view.loadUrl(url);
				}
			}
			lastUrls.add(url);
			lastUrls.remove(0);
		}
	}

	@Override
	public void onLoadResource(WebView view, String url) {
		// Log.i("onLoadResource", url);
		super.onLoadResource(view, url);
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		if (pendingUrl == null) {
			pendingUrl = url;
		}
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		if (!url.equals(pendingUrl)) {
			Log.d("Redirect(302)", "Detected HTTP redirect " + pendingUrl
					+ "->" + url);
			((EditText) act.findViewById(R.id.uText)).setText(url);
			
			// 返回首页时设置cookie
			if(url.equals(act.getString(R.string.defaultUrl))){
				HttpUtil.setCookie();
			}
			pendingUrl = null;
		}
	}
	
	@Override 
    public void onReceivedError(WebView view, int errorCode, 
            String description, String failingUrl) { 
        super.onReceivedError(view, errorCode, description, failingUrl);
        
		String htm = errorHtm.replace("#code#", String.valueOf(errorCode));
		htm = htm.replace("#url#", failingUrl);
		view.loadDataWithBaseURL(null, htm, "text/html", "utf-8", null);
    }

	/**
	 * 通过Future在指定时间内获取数据
	 */
	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
		WebResourceResponse res = null;

		FutureTask<WebResourceResponse> future = new FutureTask<WebResourceResponse>(
				new MyCallable(act, view, url));
		executor.execute(future);

		// 在这里可以做别的任何事情
		try {
			// 为1秒
			res = future.get(1, TimeUnit.SECONDS);
		} catch (Exception e) {
			future.cancel(true);
			res = null;
		}

		return res;
	}

	/**
	 * 读取文件的
	 * 
	 * @author gudh
	 * 
	 */
	class MyCallable implements Callable<WebResourceResponse> {
		private Activity act;
		private WebView view;
		private String url;

		public MyCallable(Activity act, WebView view, String url) {
			this.act = act;
			this.view = view;
			this.url = url;
		}

		public WebResourceResponse call() {
			return CacheControl.getResource(act, view, url);
		}
	}
}
