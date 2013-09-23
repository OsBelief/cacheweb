package com.yh.web.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.widget.EditText;
import android.widget.Toast;

import com.yh.web.R;
import com.yh.web.cache.CacheControl;
import com.yh.web.cache.CacheFilter;
import com.yh.web.cache.CacheObject;
import com.yh.web.cache.CachePolicy;
import com.yh.web.cache.HttpUtil;
import com.yh.web.cache.MIME;
import com.yh.web.cache.NetMonitor;
import com.yh.web.cache.ScheduleTask;
import com.yh.web.cache.StatMonitor;

public class MainActivity extends BaseActivity {

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

		// 设置WebClient
		WebView web = (WebView) findViewById(R.id.webView1);
		setWebView(web);
		
		// 初始化MIME
		MIME.initMIME(this.getAssets());
		// 初始化过滤器
		CacheFilter.initFilter(this.getAssets());
		// 初始化缓存策略
		CachePolicy.initPolicy(this.getAssets());
		// 初始化AsyncHttpClient
		HttpUtil.initAsyncHttpClient(web.getSettings().getUserAgentString());
		// 初始化缓存
		CacheControl.initCache(this);
		// 开始监控网络
		NetMonitor.startJudge();
		// 开始CPU监控
		StatMonitor.startJudge();
		// 开始执行删除过期任务
		ScheduleTask.initShedule(this);
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
		set.setAppCacheEnabled(true);// 启用缓存
		// set.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //
		// 先用缓存，缓存没有请求网络

		set.setSupportZoom(true); // 设置是否支持缩放
		set.setBuiltInZoomControls(true); // 设置是否显示内建缩放工具
		// set.setSavePassword(true); //设置是否保存密码

		// 监听长按事件
		web.setOnLongClickListener(new OnLongClickListener() {
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
		case R.id.action_download:
			// 下载URL的数据
			String url = ((EditText) findViewById(R.id.uText)).getText()
					.toString();
			String fileName = CacheObject.getCacheFileName(url);

			HttpUtil.downUrlToFile(this, url, fileName);

			return true;
		case R.id.action_settings:
			Log.d("debug", "setClick");
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
}
