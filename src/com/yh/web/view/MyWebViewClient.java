package com.yh.web.view;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Message;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import cn.yicha.cache.fuli.R;
import com.yh.web.cache.CacheControl;
import com.yh.web.cache.CacheCookieManager;
import com.yh.web.cache.HttpUtil;
import com.yh.web.cache.IOUtil;

/**
 * @author gudh 自定义浏览器WebViewClient
 */
public class MyWebViewClient extends WebViewClient {

	private ExecutorService executor;

	private MainActivity act;
	private String defaultUrl;
	
	private static String errorHtm;
	
	// 运行的最大RES次数，防止jni错误，到达指定数量时便重启
	private final static int allowMaxRes = 510;
	private final static int mainPageCount = 60;
	// 计算RES被访问多少次
	private volatile static int resUseCount = 0;
	
	// 记录302跳转情况
	private String pendingUrl;

	public MyWebViewClient(MainActivity act) {
		this.act = act;
		defaultUrl = act.getString(R.string.defaultUrl);
		executor = Executors.newSingleThreadExecutor();
		
		if(errorHtm == null){
			try {
				errorHtm = IOUtil.readStream(act.getAssets().open("error.htm"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		if(view.getUrl() == null){
			return false;
		}
		// 禁止进入首页
		if(url.startsWith("http://yicha")){
			return true;
		}
		
		Log.i("shouldOverrideUrlLoading", url);
		// 首页有65个资源，为了首页加载重启，先判断
		if(url.equals(defaultUrl) && resUseCount > allowMaxRes - mainPageCount){
			Message msg = new Message();
			msg.what = MainActivity.RESTART;
			msg.obj = view.getUrl();
			Log.i("Activity", "resUseCount:" + resUseCount + " Restart from over loading");
			act.mHandler.sendMessage(msg);
		}
		
		((EditText) act.findViewById(R.id.uText)).setText(url);
		String reload = HttpUtil.getToUrl(url);
		if (reload != null) {
			url = reload;
		}
		if(view.getUrl().equals(defaultUrl) && !url.equals(defaultUrl) && url.startsWith("http://fuli.yicha.cn")){
			// 开启一个新Activity加载
			act.startNew(url);
			return true;
		} else if(!view.getUrl().equals(defaultUrl) && url.equals(defaultUrl)){
			if(view.getUrl().startsWith("http://passport.yicha.cn/user/login")){
				CacheCookieManager.setCookieChanged(url, true);
				// act.exitAndStartNew(url);
			}
			// finish当前，返回
			if(!act.tUrl.equals(defaultUrl)){
				if(MainActivity.canFinish){
					act.finish();
					return true;
				} else{
					// 置为主Activity
					act.tUrl = defaultUrl;
				}
			}
		}
		// return false 交给原生处理
		return false;
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
			pendingUrl = null;
		}
		if(url.equals(defaultUrl)){
			// 已经完成了，设置已经更新
			CacheCookieManager.setCookieChanged(url, false);
			// 存储主页内容
			view.loadUrl("javascript:(function(){window.htmif.callbackPageInfo(window.location.href, document.doctype.nextSibling.outerHTML)})()");
		} else{
			// 修改返回功能
			String goBackBind = "javascript:(function(){window.history.go=function(k){window.jsif.historyGo(k)};})()";
			view.loadUrl(goBackBind);
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
			if(res != null){
				Log.i("ResCount", String.valueOf(resUseCount++));
				if(resUseCount == allowMaxRes){
					// 重启防止jni错误
					Message msg = new Message();
					msg.what = MainActivity.RESTART;
					msg.obj = view.getUrl();
					Log.i("Activity", "resUseCount:" + resUseCount + " Restart from shouldInterceptRequest");
					act.mHandler.sendMessage(msg);
				}
			}
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
