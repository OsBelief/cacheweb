package com.yh.web.view;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

import cn.yicha.cache.fuli.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;

/**
 * 启动时Activity
 * 
 * @author gudh
 * @data 2013-11-29
 */
public class WelcomeActivity extends BaseActivity {
	private static final int GO_HOME = 1000;
	private static final int GO_GUIDE = 1001;
	private static final int GO_COPY = 1002;
	private static final int INIT_DATA = 1003;
	
	// 延迟1秒
	private static final long DELAY_MILLIS = 1000;

	public static final String FIRSTSTART_PREF = "first_start";
	public static final String FIRSTSTART_KEY = "is_first";

	private static final String baseUA = "yicha.cache.fuli_1.0";
	public static String UA = baseUA;
	
	public volatile static boolean initData = false;
	
	/**
	 * Handler:跳转到不同界面
	 */
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GO_COPY:
				copyInitFile();
				break;
			case GO_HOME:
				goActivity(MainActivity.class);
				break;
			case GO_GUIDE:
				goActivity(GuideActivity.class);
				break;
			case INIT_DATA:
				initFirstDatas();
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);

		init();
	}

	/**
	 * 判断网络是否可用，并进入主界面
	 */
	public void init(){
		if(NetMonitor.isNetworkAvailable(this)){
			initEntry();
		}else{
			showNetUnavailable();
		}
	}
	
	/**
	 * 进入
	 */
	private void initEntry() {
		// 判断是否是第一次运行
		SharedPreferences preferences = getSharedPreferences(
				FIRSTSTART_PREF, MODE_PRIVATE);
		boolean isFirstIn = preferences.getBoolean(FIRSTSTART_KEY, true);

		if (!isFirstIn) {
			// 使用Handler的postDelayed方法，1秒后执行跳转到MainActivity
			mHandler.sendEmptyMessageDelayed(GO_HOME, DELAY_MILLIS);
		} else {
			// 开始拷贝
			copyInitFile();
		}
		mHandler.sendEmptyMessageDelayed(INIT_DATA, 1);
	}
	
	/**
	 * 拷贝初始化文件到sd卡
	 */
	private void copyInitFile() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					char[] xx = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
							'9', 'a', 'b', 'c', 'd', 'e', 'f' };
					for (char x : xx) {
						String base = "cfile/" + x;
						String[] cfiles = WelcomeActivity.this.getAssets().list(base);
						base = base + "/";
						for (String file : cfiles) {
							String tofile = CacheObject.rootPath + base + file;
							Log.d("InitFile", "copy to " + tofile);
							IOUtil.writeExternalFile(tofile, WelcomeActivity.this.getAssets()
									.open(base + file));
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				// 进入引导页
				WelcomeActivity.this.mHandler.sendEmptyMessageDelayed(GO_GUIDE, 0);
			}
		}).start();
	}
	
	/**
	 * 初始数据
	 */
	private void initFirstDatas(){
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
		
		initDatas(this);
	}
	
	/**
	 * 初始化信息
	 */
	public synchronized static void initDatas(Activity activity){
		if(initData){
			return;
		}
		
		UA = getUserAgent(activity, baseUA);
		
		// 通用线程池
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 2, 60,
				TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		ScheduledExecutorService monitorThreadPool = Executors
				.newScheduledThreadPool(1);

		// 初始化MIME
		MIME.initMIME(activity);
		// 初始化过滤器
		CacheFilter.initFilter(activity);
		// 初始化缓存策略
		CachePolicy.initPolicy(activity);
		// 初始化AsyncHttpClient
		// HttpUtil.initAsyncHttpClient(web.getSettings().getUserAgentString());
		HttpUtil.initAsyncHttpClient(activity, threadPool, UA + "_hc");
		// 初始化Cookie信息
		// CookieManagers.initCookieManager(activity);
		// 初始化缓存
		CacheControl.initCache(activity);
		// 开始监控网络
		NetMonitor.startJudge(monitorThreadPool);
		// 开始CPU监控
		StatMonitor.startJudge(monitorThreadPool);
		// 开始执行删除过期任务
		DeleteTask.initShedule(activity, monitorThreadPool);

		UpdateTask.initBasic(activity);
		// 开始执行更新配置任务
		// UpdateTask.initShedule(activity);
		
		initData = true;
	}
	
	/**
	 * 获取UA
	 * @param activity
	 * @param baseUa
	 * @return
	 */
	public static String getUserAgent(Activity activity, String baseUa){
		TelephonyManager tm = (TelephonyManager) activity.getBaseContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		String ua = new StringBuffer().append(baseUa).append(",")
				.append(android.os.Build.MODEL).append(",")
				.append(android.os.Build.VERSION.SDK_INT).append(",")
				.append(android.os.Build.VERSION.RELEASE).append(",")
				.append(tm.getDeviceId()).toString().replaceAll(" +", "_");
		return ua;
	}
	
	/**
	 * 显示网络不可用
	 */
	private void showNetUnavailable() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("当前网络不可用，易查福利需要在有网络的情况下访问，请检查网络重试或退出。")
				.setTitle("网络连接提示")
				.setPositiveButton("重试", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
		                    field.setAccessible(true);   
		                    field.set(dialog, false);
						} catch (NoSuchFieldException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}   
						init();
						
					}
				})
				.setNegativeButton("退出", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						BaseActivity.exit();
					}
				}).setCancelable(false).create().show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.welcome, menu);
		return true;
	}

	private void goActivity(Class<?> cls) {
		Intent intent = new Intent(WelcomeActivity.this, cls);
		WelcomeActivity.this.startActivity(intent);
		// WelcomeActivity.this.finish();
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}
}
