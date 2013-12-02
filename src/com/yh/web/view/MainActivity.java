package com.yh.web.view;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.widget.EditText;
import android.widget.Toast;

import cn.yicha.cache.fuli.R;
import com.yh.web.cache.CacheControl;
import com.yh.web.cache.CacheFilter;
import com.yh.web.cache.CacheObject;
import com.yh.web.cache.CachePolicy;
import com.yh.web.cache.DeleteTask;
import com.yh.web.cache.HttpUtil;
import com.yh.web.cache.IOUtil;
import com.yh.web.cache.MIME;
import com.yh.web.cache.NetMonitor;
import com.yh.web.cache.StatMonitor;
import com.yh.web.cache.UpdateTask;

public class MainActivity extends BaseActivity {

	private ThreadPoolExecutor threadPool;
	private ScheduledExecutorService monitorThreadPool;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 第一次启动时将所有配置文件删除
		if (IOUtil.readBooleanKeyValue(this, "FirstLaunch", true)) {
			this.deleteFile(CacheFilter.CONFIG_NAME);
			this.deleteFile(CacheFilter.FILTER_NAME);
			this.deleteFile(CachePolicy.POLICY_NAME);
			this.deleteFile(MIME.MIME_NAME);
			IOUtil.writeBooleanKeyValue(this, "FirstLaunch", false);
			Log.i("OnCreate",
					"application is first launch, delete the config file");
		} else {
			Log.i("OnCreate", "application is not first launch");
		}

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

		// 设置WebClient
		WebView web = (WebView) findViewById(R.id.webView1);
		setWebView(web);

		// // 获取焦点隐藏地址栏
		// ArrayList<View> views = new ArrayList<View>();
		// views.add(uText);
		// views.add(findViewById(R.id.goBtn));
		// web.setOnFocusChangeListener(new MyFoucusChange(views));
		
		// 通用线程池
		threadPool = new ThreadPoolExecutor(1, 2, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		monitorThreadPool = Executors.newScheduledThreadPool(1);
		
		// 初始化MIME
		MIME.initMIME(this);
		// 初始化过滤器
		CacheFilter.initFilter(this);
		// 初始化缓存策略
		CachePolicy.initPolicy(this);
		// 初始化AsyncHttpClient
		// HttpUtil.initAsyncHttpClient(web.getSettings().getUserAgentString());
		HttpUtil.initAsyncHttpClient(this, threadPool, "yiccha.cache.fuli_1.0");
		// 初始化缓存
		CacheControl.initCache(this);
		// 开始监控网络
		NetMonitor.startJudge(monitorThreadPool);
		// 开始CPU监控
		StatMonitor.startJudge(monitorThreadPool);
		// 开始执行删除过期任务
		DeleteTask.initShedule(this, monitorThreadPool);
		
		UpdateTask.initBasic(this);
		// 开始执行更新配置任务
		// UpdateTask.initShedule(this);
		
		web.loadUrl(this.getString(R.string.defaultUrl));
	}

	/**
	 * 设置Web信息
	 * 
	 * @param web
	 */
	@SuppressLint("SetJavaScriptEnabled")
	public void setWebView(WebView web) {
		web.setWebViewClient(new MyWebViewClient(this));
		web.setWebChromeClient(new MyWebChromeClient(this));

		WebSettings set = web.getSettings();
		set.setJavaScriptEnabled(true);// 启用JS

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
			} else {
				showExitDialog();
				return true;
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
}
