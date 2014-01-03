package com.yh.web.view;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.umeng.analytics.MobclickAgent;
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
import android.os.Environment;
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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;

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
	private static final int UPDATE_PROCESS = 1004;

	// 延迟1秒
	private static final long DELAY_MILLIS = 1000;

	public static final String FIRSTSTART_PREF = "first_start";
	public static final String FIRSTSTART_KEY = "is_first";

	private static final String baseUA = "yc_app_android,cache.fuli_";
	public static String UA = baseUA;

	public volatile static boolean initData = false;
	
	private ProgressBar copyPB;

	/**
	 * Handler:跳转到不同界面
	 */
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_PROCESS:
				copyPB.setProgress(msg.arg1);
				break;
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

		Intent intent = getIntent();
		boolean ref = intent.getBooleanExtra(MainActivity.REFRESH_KEY, false);
		if (ref) {
			// 重新启动时传入参数
			Intent intent1 = new Intent(WelcomeActivity.this,
					MainActivity.class);
			intent1.putExtras(intent);
			WelcomeActivity.this.startActivity(intent1);
		} else {
			setContentView(R.layout.activity_welcome);
			init();
		}
	}

	/**
	 * 判断网络是否可用，并进入主界面
	 */
	public void init() {
		if (NetMonitor.isNetworkAvailable(this)) {
			initEntry();
		} else {
			showNetUnavailable();
		}
	}

	/**
	 * 进入
	 */
	private void initEntry() {
		// 判断是否是第一次运行
		SharedPreferences preferences = getSharedPreferences(FIRSTSTART_PREF,
				MODE_PRIVATE);
		boolean isFirstIn = preferences.getBoolean(FIRSTSTART_KEY, true);
		// 程序仅在第一次安装启动时用缓存main.htm
		// MainActivity.isFirst = isFirstIn;
		//检查SD卡是否可用
		String state = Environment.getExternalStorageState();
		if(Environment.MEDIA_MOUNTED.equals(state)) {
			if (!isFirstIn) {
				// 使用Handler的postDelayed方法，1秒后执行跳转到MainActivity
				mHandler.sendEmptyMessageDelayed(GO_HOME, DELAY_MILLIS);
			} else {
				// 开始拷贝
				copyPB = (ProgressBar) findViewById(R.id.progressbar_copy);
				copyPB.setVisibility(View.VISIBLE);
				copyPB.setProgress(0);
				copyInitFile();
			}
		} else {
			showSDUnavailable();
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
					int sum = 0;
					for (char x : xx) {
						String base = "cfile/" + x;
						String[] cfiles = WelcomeActivity.this.getAssets()
								.list(base);
						sum += cfiles.length;
					}
					Log.i("the number of files", String.valueOf(sum));
					copyPB.setMax(sum);
					int progress = 0;
					for (char x : xx) {
						String base = "cfile/" + x;
						String[] cfiles = WelcomeActivity.this.getAssets()
								.list(base);
						base = base + "/";
						for (String file : cfiles) {
							String tofile = CacheObject.rootPath + base + file;
							Log.d("InitFile", "copy to " + tofile);
							IOUtil.writeExternalFile(
									tofile,
									WelcomeActivity.this.getAssets().open(
											base + file));
							Message msg = mHandler.obtainMessage();
							msg.what = UPDATE_PROCESS;
							msg.arg1 = ++progress;
							mHandler.sendMessage(msg);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				// 进入引导页
				WelcomeActivity.this.mHandler.sendEmptyMessageDelayed(GO_GUIDE,
						0);
			}
		}).start();
	}

	/**
	 * 初始数据
	 */
	private void initFirstDatas() {
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

		initDatas(this, getNowVersion());
	}

	/**
	 * 获取版本号
	 * 
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

	/**
	 * 初始化信息
	 */
	public synchronized static void initDatas(Activity activity,
			String nowVersion) {
		if (initData) {
			return;
		}

		UA = getUserAgent(activity, baseUA, nowVersion);

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

		UpdateTask.initBasic(activity, UA + "_update_hc");
		// 开始执行更新配置任务
		// UpdateTask.initShedule(activity);

		initData = true;
	}

	/**
	 * 获取UA
	 * 
	 * @param activity
	 * @param baseUa
	 * @return
	 */
	public static String getUserAgent(Activity activity, String baseUa,
			String nowVersion) {
		TelephonyManager tm = (TelephonyManager) activity.getBaseContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		String ua = new StringBuffer().append(baseUa).append(nowVersion)
				.append(",").append(android.os.Build.MODEL).append(",")
				.append(android.os.Build.VERSION.SDK_INT).append(",")
				.append(android.os.Build.VERSION.RELEASE).append(",#")
				.append(tm.getDeviceId()).append("#").toString()
				.replaceAll(" +", "_");
		return ua;
	}
	/**
	 * 显示SD卡不可用
	 */
	private void showSDUnavailable() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("SD卡不可用，易查福利需要SD卡的支持，请插入SD卡后重试。")
				.setTitle("SD卡读写异常")
				.setNegativeButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						BaseActivity.exit();
					}
				}).setCancelable(false).create().show();
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
							Field field = dialog.getClass().getSuperclass()
									.getDeclaredField("mShowing");
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

	/**
	 * 友盟统计
	 */
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	/**
	 * 友盟统计
	 */
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
