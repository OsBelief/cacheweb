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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.widget.EditText;
import android.widget.Toast;
import cn.yicha.cache.fuli.R;

import com.umeng.analytics.MobclickAgent;
import com.yh.util.ScreenShot;
import com.yh.web.cache.CacheCookieManager;
import com.yh.web.cache.HttpUtil;
import com.yh.web.cache.UpdateTask;

public class MainActivity extends BaseActivity {
	
	public static final int SHOT = 1010;
	public static final String DEFAULT_URL = "http://fuli.yicha.cn/fuli/index";
	public static final String URL_KEY = "IURL";
	public static final String REFRESH_KEY = "REFRESH";
	protected static final int HISTORY_GO = 1013;
	public static final int RESTART = 1014;
	public static final int NEWVERSION = 1015;
	public static final int UPDATE_CONFIG = 1016;
	protected static final int SHOW_DIALGO = 1017;
	
	// 初始时的URL
	public String tUrl;
	public WebView web;
	
	private static MyJsInterface jsif = new MyJsInterface();
	private static HtmlInterface htmif = new HtmlInterface();
	
	// 记录是否是第一次
	public static boolean isFirst = false;
	
	// 记录当前主页所在Activity
	public static MainActivity nowMainAct;
	
	public static boolean canFinish = false;
	
	@SuppressLint("HandlerLeak")
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOT:
				ScreenShot.shotOneBitmap();
				break;
			case HISTORY_GO:
				if(web != null){
					if(web.canGoBack()){
						int steps = msg.arg1;
						web.goBackOrForward(steps);
					}
				}
				break;
			case RESTART:
				String url = DEFAULT_URL;
				Object obj = msg.obj;
				if(obj != null){
					url = (String)obj;
				}
				exitAndStartNew(url);
				break;
			case NEWVERSION:
				String[] newVersionInfos = (String[]) msg.obj;
				callBackUpdateDialog(newVersionInfos, msg.arg1);
				break;
			case UPDATE_CONFIG:
				String strMsg = (String) msg.obj;
				Toast.makeText(MainActivity.this, strMsg, Toast.LENGTH_LONG).show();
				break;
			case SHOW_DIALGO:
				Builder builder = (Builder) msg.obj;
				builder.create().show();
				break;
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
		WelcomeActivity.initDatas(this, getNowVersion());
		
		tUrl = getIntent().getStringExtra(URL_KEY);
		if(tUrl == null){
			tUrl = DEFAULT_URL;
		}
		if(tUrl.equals(DEFAULT_URL)){
			// 添加JS接口
			jsif.setFirstActivity(this);
			nowMainAct = this;
			
			// 第一次启动时检测版本更新
			UpdateTask.checkNewestVersion(this, 0);
			// 更新配置
			UpdateTask.updateOneTime(this, 0);
		} else{
			// 添加JS接口
			jsif.setSecondActivity(this);
		}
		
		// 设置WebClient
		web = (WebView) findViewById(R.id.webView1);
		setWebView(web, WelcomeActivity.UA);
		
		boolean refresh = getIntent().getBooleanExtra(REFRESH_KEY, false);
		if(refresh){
			CacheCookieManager.setCookieChanged(DEFAULT_URL, true);
			isFirst = false;
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if(web.getUrl() == null || CacheCookieManager.isCookieChanged(tUrl)){
			web.loadUrl(tUrl);
		}
		MyWebChromeClient.setRefreshActivity(this);
		
		/**
		 * 友盟统计
		 */
		MobclickAgent.onResume(this);
	}
	
	/**
	 * 友盟统计
	 */
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	@Override
	public void finish(){
		canFinish = false;
		Log.i("Acitivty", "update canFinish: " + canFinish);
		MyWebChromeClient.setRefreshActivity(null);
		super.finish();
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
		// set.setBuiltInZoomControls(true); // 设置是否显示内建缩放工具
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
		// 监听下载文件
		web.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
		
		// web 获得焦点
		web.requestFocus();
		
		// 添加java js事件
		web.addJavascriptInterface(jsif, "jsif");
		if(DEFAULT_URL.equals(tUrl)){
			web.addJavascriptInterface(htmif, "htmif");
		}
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
			// 显示主页
			if(canFinish){
				this.finish();
			} else{
				CacheCookieManager.setCookieChanged(DEFAULT_URL, true);
				web.loadUrl(DEFAULT_URL);
			}
			return true;
		case R.id.action_updateconfig:
			// 更新配置
			UpdateTask.updateOneTime(this, 1);
			return true;
		case R.id.action_checkversion:
			// 检查版本更新
			UpdateTask.checkNewestVersion(this, 1);
			// 更新配置
			UpdateTask.updateOneTime(this, 0);
			return true;
		case R.id.action_showhide:
			// 显示或隐藏地址栏
			if (findViewById(R.id.uText).getVisibility() == View.GONE) {
				findViewById(R.id.uText).setVisibility(View.VISIBLE);
				findViewById(R.id.goBtn).setVisibility(View.VISIBLE);
			} else {
				findViewById(R.id.uText).setVisibility(View.GONE);
				findViewById(R.id.goBtn).setVisibility(View.GONE);
			}
			return true;
		case R.id.action_about:
			// 关于
			about();
			return true;
		case R.id.action_eixt:
			// 退出
			exit();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * 显示关于提示
	 */
	protected void about() {
		Builder builder = new Builder(this);
		builder.setMessage("	易查福利客户端，是基于缓存开发的一个客户端，相比浏览器能节省流量，加快访问速度，方便启动和管理。\n\n	欢迎你的使用！");
		builder.setTitle("易查福利客户端");
		builder.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setIcon(R.drawable.info);
		builder.create().show();
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
		builder.setIcon(R.drawable.confirm);
		builder.create().show();
	}

	/**
	 * 显示更新对话框
	 * @param versionInfos
	 * @param needShow
	 */
	protected void callBackUpdateDialog(String[] versionInfos, int needShow) {
		if(versionInfos != null){
			String version = getNowVersion();
			if(version.equals(versionInfos[0])){
				// 版本一致，则设更新信息为null
				versionInfos = null;
			}
		}
		if(needShow == 0 && versionInfos == null){
			// 如果第一次运行，并且没有更新则不提示了
			return;
		}
		
		Builder builder = new Builder(this);
		builder.setTitle("易查福利更新");
		if(versionInfos == null){
			builder.setMessage("当前系统是最新版，不需要更新。");
			builder.setPositiveButton("确认", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		} else{
			builder.setMessage(versionInfos[1]);
			final String url = versionInfos[2];
			builder.setPositiveButton("立即更新", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					MainActivity.this.web.loadUrl(url);
					dialog.dismiss();
				}
			});
			builder.setNegativeButton("稍候更新", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		}
		builder.setIcon(R.drawable.confirm);
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
			if (web.canGoBack() && !DEFAULT_URL.equals(web.getUrl())) {
				web.goBack();
				((EditText) findViewById(R.id.uText)).setText(web.getUrl());
				return true;
			} else if(DEFAULT_URL.equals(web.getUrl())){
				showExitDialog();
				return true;
			} else if(!canFinish){
				// 位于栈底返回直接加载主页
				this.tUrl = DEFAULT_URL;
				web.loadUrl(DEFAULT_URL);
				return true;
			} else {
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
		intent.putExtra(REFRESH_KEY, false);
		this.startActivity(intent);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		
		// 有启动新的Activity则可以finish了
		canFinish = true;
		Log.i("Acitivty", "update canFinish: " + canFinish);
//		web.clearCache(false);
//		web.loadUrl("about:blank"); //web.clearView();
//		web.destroyDrawingCache();
//		web.destroy();
//		this.finish();
//		this.onDestroy();
	}
	
	public void exitAndStartNew(String url){
		Log.i("Activity", "start a new activity :" + url);
//		Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);
//		intent.putExtra(URL_KEY, url);
//		intent.putExtra(REFRESH_KEY, true);
//		// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		this.getApplicationContext().startActivity(intent);
		Intent intent = getBaseContext().getPackageManager()
				.getLaunchIntentForPackage(getBaseContext().getPackageName());
		intent.putExtra(URL_KEY, url);
		intent.putExtra(REFRESH_KEY, true);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		// for restarting the Activity
		BaseActivity.exit();
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}
	
	/**
	 * 获取版本号
	 * @return
	 */
	public String getNowVersion() {
	    try {
	        PackageManager manager = this.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
	        String version = info.versionName;
	        return version;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return this.getString(R.string.can_not_find_version_name);
	    }
	}
}
