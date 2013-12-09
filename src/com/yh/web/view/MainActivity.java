package com.yh.web.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.widget.EditText;
import android.widget.Toast;
import cn.yicha.cache.fuli.R;

import com.yh.util.ScreenShot;
import com.yh.web.cache.CacheObject;
import com.yh.web.cache.HttpUtil;
import com.yh.web.cache.UpdateTask;

public class MainActivity extends BaseActivity {
	
	public static final int SHOT = 1010;
	public static final String DEFAULT_URL = "http://fuli.yicha.cn/fuli/index";
	private static final String URL_KEY = "IURL";
	protected static final int SETCOOKIE = 1012;
	
	// 初始时的URL
	public String tUrl;
	private WebView web;
	
	private static MyJsInterface jsif = new MyJsInterface();
	
	@SuppressLint("HandlerLeak")
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOT:
				ScreenShot.shotOneBitmap();
				break;
			case SETCOOKIE:
				HttpUtil.setCookie();
			} 
			super.handleMessage(msg);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 添加事件，点击GO的时候自动调用GoBtn方法跳到指定URL
		EditText uText = (EditText) findViewById(R.id.uText);
		// 安装回车自动加载
		uText.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					goBtnClick(null);
					return true;
				}
				return false;
			}
		});

		// 初始化基本信息
		WelcomeActivity.initDatas(this);
		
		// 设置WebClient
		web = (WebView) findViewById(R.id.webView1);
		setWebView(web, WelcomeActivity.UA);
		tUrl = getIntent().getStringExtra(URL_KEY);
		if(tUrl == null){
			tUrl = DEFAULT_URL;
		}
		if(tUrl.equals(DEFAULT_URL)){
			mHandler.sendEmptyMessageDelayed(SETCOOKIE, 1000);
			// 添加JS接口
			jsif.setFirstActivity(this);
		} else{
			// 添加JS接口
			jsif.setSecondActivity(this);
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if(web.getUrl() == null || HttpUtil.isCookieChanged(tUrl)){
			web.loadUrl(tUrl);
		}
	}
	
	/**
	 * 设置Web信息
	 * 
	 * @param web
	 */
	@SuppressLint("SetJavaScriptEnabled")
	public void setWebView(WebView web, String ua) {
		web.setWebViewClient(new MyWebViewClient(this));
		web.setWebChromeClient(new MyWebChromeClient(this));

		WebSettings set = web.getSettings();
		set.setJavaScriptEnabled(true);// 启用JS
		set.setUserAgentString(ua);

		set.setDomStorageEnabled(true);// 启用localStorage
		String path = this.getApplicationContext()
				.getDir("databases", Context.MODE_PRIVATE).getPath();
		Log.d("SetPath", "databases " + path);
		set.setDatabasePath(path); // 设置路径

		set.setAppCacheEnabled(true);// 启用缓存
		path = this.getApplicationContext()
				.getDir("cache", Context.MODE_PRIVATE).getPath();
		Log.d("SetPath", "cache " + path);
		set.setAppCachePath(path);

		// set.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //
		// 先用缓存，缓存没有请求网络

		set.setSupportZoom(true); // 设置是否支持缩放
		set.setBuiltInZoomControls(true); // 设置是否显示内建缩放工具
		// set.setSavePassword(true); //设置是否保存密码

		// 监听长按事件
		web.setOnLongClickListener(new OnLongClickListener() {
			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@Override
			public boolean onLongClick(View view) {
				HitTestResult result = ((WebView) view).getHitTestResult();
				if (result.getType() == WebView.HitTestResult.IMAGE_TYPE) {
					// 处理长按图片的菜单项
					ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData textCd = ClipData.newPlainText("ImageUrl",
							result.getExtra());
					clipboard.setPrimaryClip(textCd);
					Toast.makeText(view.getContext(), "图片URL已拷贝",
							Toast.LENGTH_LONG).show();
					return true;
				}
				return false;
			}
		});
		// web 获得焦点
		web.requestFocus();
		
		// 添加java js事件
		web.addJavascriptInterface(jsif, "jsif");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * 菜单选择处理
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_mainpage:
			WebView web = (WebView) findViewById(R.id.webView1);
			web.loadUrl(this.getString(R.string.defaultUrl));
			return true;
		case R.id.action_updateconfig:
			UpdateTask.updateOneTime();
//			if(ScreenShot.startOrEndShot(this, monitorThreadPool)){
//				Toast.makeText(this, "开始截图", Toast.LENGTH_SHORT).show();
//			}else{
//				Toast.makeText(this, "停止截图", Toast.LENGTH_SHORT).show();
//			}
			return true;
		case R.id.action_download:
			// 下载URL的数据
			String url = ((EditText) findViewById(R.id.uText)).getText()
					.toString();
			String fileName = CacheObject.getCacheFileName(url);

			HttpUtil.downUrlToFile(this, url, fileName);

			return true;
		case R.id.action_settings:
			if (findViewById(R.id.uText).getVisibility() == View.GONE) {
				findViewById(R.id.uText).setVisibility(View.VISIBLE);
				findViewById(R.id.goBtn).setVisibility(View.VISIBLE);
			} else {
				findViewById(R.id.uText).setVisibility(View.GONE);
				findViewById(R.id.goBtn).setVisibility(View.GONE);
			}
			return true;
		case R.id.action_eixt:
			exit();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * 显示退出提示
	 */
	protected void showExitDialog() {
		Builder builder = new Builder(this);
		builder.setMessage("确认退出吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				exit();
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	/**
	 * 单击Go按钮
	 * 
	 * @param view
	 * @return
	 */
	public boolean goBtnClick(View view) {
		EditText uText = (EditText) findViewById(R.id.uText);
		String url = uText.getText().toString();
		if (HttpUtil.isUrl(url)) {
			if (!url.startsWith("http://") && !url.startsWith("https://")) {
				url = "http://" + url;
			}
		} else {
			url = "http://www.google.com.hk/search?q=" + url;
		}
		uText.setText(url);
		WebView web = (WebView) findViewById(R.id.webView1);
		web.loadUrl(url);
		web.requestFocus();
		return true;
	}

	/**
	 * 捕捉返回键处理
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		WebView web = (WebView) findViewById(R.id.webView1);
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (web.canGoBack()) {
				web.goBack();
				((EditText) findViewById(R.id.uText)).setText(web.getUrl());
				return true;
			} else if(DEFAULT_URL.equals(web.getUrl())){
				showExitDialog();
				return true;
			} else{
				this.finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	class MyFoucusChange implements OnFocusChangeListener {
		List<View> views = new ArrayList<View>();

		public MyFoucusChange(List<View> views) {
			this.views = views;
		}

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {// 已经获得焦点
				for (View view : views) {
					view.setVisibility(View.GONE);
				}
			}
		}
	}

	// 文件上传支持
	public ValueCallback<Uri> mUploadMessage;
	public final static int FILECHOOSER_RESULTCODE = 1;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	        Intent intent) {
	    if (requestCode == FILECHOOSER_RESULTCODE) {
	        if (null == mUploadMessage)
	            return;
	        Uri result = intent == null || resultCode != RESULT_OK ? null
	                : intent.getData();
	        mUploadMessage.onReceiveValue(result);
	        mUploadMessage = null;
	    }
	}

	// 开启一个新的Activity来加载URL
	public void startNew(String url) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(URL_KEY, url);
		this.startActivity(intent);
		
//		web.clearCache(false);
//		web.loadUrl("about:blank"); //web.clearView();
//		web.destroyDrawingCache();
//		web.destroy();
//		this.finish();
//		this.onDestroy();
	}
	
	public void exitAndStartNew(String url){
		Log.i("Activity", "start a new activity :" + url);
		Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);
		intent.putExtra(URL_KEY, url);
		intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.getApplicationContext().startActivity(intent);
		// for restarting the Activity
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}
}
