package com.yh.web.cache;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;

import com.yh.web.view.MainActivity;

import android.content.Context;
import android.os.Message;
import android.util.Log;

public class UpdateTask {

	// 配置文件地址
	private static String configUrl = "http://fuli.yicha.cn/cache/config.txt";
	private static final String NEWEST_URL = "http://fuli.yicha.cn/cache/new.txt";

	private static long defaultSleepTime = 600000;
	private static long sleepTime = 10000;
	private static boolean runFlag = false;
	private static HttpClient getClient;
	private static Context context;

	// 只需更新，不许内存处理的数据
	private static String[] updateList = new String[] { "main.htm" };

	/**
	 * 初始化基本数据
	 */
	public static void initBasic(Context context, String ua){
		getClient = new DefaultHttpClient();
		HttpProtocolParams.setUserAgent(getClient.getParams(), ua);
		ConnManagerParams.setTimeout(getClient.getParams(), 5000);
		UpdateTask.context = context;
	}
	
	/**
	 * 以默认参数初始化，每次延时600s
	 * 
	 * @param context
	 */
	public static void initShedule(Context context, String ua) {
		initBasic(context, ua);
		// 开始任务
		startUpdateTask();
	}

	/**
	 * 初始化orm和定时任务
	 * 
	 * @param context
	 *            用于初始化数据库ORM
	 * @param sleepTime
	 *            单位ms，每次删除延时，至少1000ms
	 */
	public static void initShedule(Context context, String ua, long sleepTime) {
		initBasic(context, ua);
		if (sleepTime >= 1000) {
			UpdateTask.sleepTime = sleepTime;
		}
		// 开始任务
		startUpdateTask();
	}

	/**
	 * 启动线程运行任务
	 * 
	 * @param everyDelCount
	 * @param sleepTime
	 * @return
	 */
	public static void startUpdateTask() {
		runFlag = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				long start, end;
				while (runFlag) {
					// 如果网络繁忙则咱不进行更新操作
					if (!NetMonitor.isNetBuzy() && !StatMonitor.isCPUBuzy()) {
						start = System.currentTimeMillis();
						try {
							updateConfig();
						} catch (Exception e) {
							Log.e("UpdateTask", e.getMessage());
						}
						end = System.currentTimeMillis();
						Log.i("UpdateTask", "update use time : "
								+ (end - start));
					} else {
						Log.d("UpdateTask",
								"Net or CPU is buzy, pass update config");
					}

					try {
						Thread.sleep(UpdateTask.sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * 停止任务
	 */
	public static void stopUpdateTask() {
		runFlag = false;
	}

	private volatile static boolean isUpdate = false;
	/**
	 * 启动一个线程更新配置
	 * 
	 * @param activity
	 * @param arg1 为0不显示提示，为1显示提示
	 */
	public static void updateOneTime(final MainActivity activity, final int arg1) {
		if(isUpdate){
			Message msg = new Message();
			msg.what = MainActivity.UPDATE_CONFIG;
			msg.obj = "后台正在更新，请稍候再试。";
			activity.mHandler.sendMessage(msg);
			return;
		}
		isUpdate = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				long start = System.currentTimeMillis();
				Message msg = new Message();
				msg.what = MainActivity.UPDATE_CONFIG;
				try {
					UpdateTask.updateConfig();
					msg.obj = "更新成功";
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("UpdateTask", e.getMessage());
					msg.obj = "更新失败" + e.getMessage();
				}
				if(arg1 == 1){
					activity.mHandler.sendMessage(msg);
				}
				long end = System.currentTimeMillis();
				Log.i("UpdateTask", "update use time : " + (end - start));
				UpdateTask.isUpdate = false;
			}
		}).start();
	}

	/**
	 * 执行更新配置操作
	 * @throws Exception 
	 */
	public static boolean updateConfig() throws Exception {
		boolean res = false;
		List<String[]> needUpdate = getNeedUpdate();
		// 是否含有sleepTime
		boolean hasSleep = false;

		// 存储删除缓存的where语句
		List<String> delWheres = new ArrayList<String>(needUpdate.size());

		for (String[] nameUrl : needUpdate) {
			if (nameUrl[0].equals(CacheFilter.FILTER_NAME)) {
				String content = getStringFromUrl(nameUrl[1]);
				if (content == null) {
					continue;
				}
				// 更新成功后写入文件
				if (CacheFilter.initFilter(content)) {
					IOUtil.writeInternalFile(context, nameUrl[0],
							content.getBytes("utf-8"));
					Log.i("UpdateConfig", "update " + nameUrl[0] + " ok");
				} else {
					Log.i("UpdateConfig", "update " + nameUrl[0] + " fail");
				}
			} else if (nameUrl[0].equals(CachePolicy.POLICY_NAME)) {
				String content = getStringFromUrl(nameUrl[1]);
				if (content == null) {
					continue;
				}
				// 更新成功后写入文件
				if (CachePolicy.initPolicy(content)) {
					IOUtil.writeInternalFile(context, nameUrl[0],
							content.getBytes("utf-8"));
					Log.i("UpdateConfig", "update " + nameUrl[0] + " ok");
				} else {
					Log.i("UpdateConfig", "update " + nameUrl[0] + " fail");
				}
			} else if (nameUrl[0].equals(MIME.MIME_NAME)) {
				String content = getStringFromUrl(nameUrl[1]);
				if (content == null) {
					continue;
				}
				// 更新成功后写入文件
				if (MIME.initMIME(content)) {
					IOUtil.writeInternalFile(context, nameUrl[0],
							content.getBytes("utf-8"));
					Log.i("UpdateConfig", "update " + nameUrl[0] + " ok");
				} else {
					Log.i("UpdateConfig", "update " + nameUrl[0] + " fail");
				}
			} else if (nameUrl[0].equals("sleepTime")) {
				try {
					sleepTime = Long.parseLong(nameUrl[1]);
					hasSleep = true;
					Log.i("UpdateConfig", "update " + nameUrl[0] + " "
							+ nameUrl[1]);
				} catch (Exception e) {
				}
			} else if (nameUrl[0].equals("delWhere")) {
				// 删除缓存
				delWheres.add(nameUrl[1]);
				Log.i("UpdateConfig", "delWhere | " + nameUrl[1]);
			} else {
				boolean use = false;
				for (String update : updateList) {
					if (nameUrl[0].equals(update)) {
						String content = getStringFromUrl(nameUrl[1]);
						IOUtil.writeInternalFile(context, nameUrl[0],
								content.getBytes("utf-8"));
						use = true;
						Log.i("UpdateConfig", "updateList | " + nameUrl[1]);
						break;
					}
				}
				if (!use) {
					Log.w("UpdateConfig", "not fount nameUrl " + nameUrl[0]
							+ " " + nameUrl[1]);
				}
			}
		}
		if (!hasSleep) {
			sleepTime = defaultSleepTime;
		}

		// 删除指定URL的缓存
		if (delWheres.size() > 0) {
			boolean delRes = true;
			List<CacheObject> delObjs = DeleteTask
					.getNeedDeleteUrlCache(delWheres);
			if (delObjs.size() > 0) {
				delRes = DeleteTask.deleteExpireCache(delObjs);
			}
			Log.i("DelUrls",
					"delete url " + delRes + " | count " + delObjs.size());
		}

		return res;
	}

	/**
	 * 获取需要更新的信息
	 * 
	 * @return
	 * @throws Exception 
	 */
	private static List<String[]> getNeedUpdate() throws Exception {
		List<String[]> needUpdate = new ArrayList<String[]>();

		String config = getStringFromUrl(configUrl);
		if (config != null) {
			String[] lines = config.split("\n");
			for (String line : lines) {
				if (line.startsWith("#") || line.trim().equals("")) {
					continue;
				}
				String infos[] = line.split("---");
				if (infos.length != 2) {
					continue;
				}
				needUpdate.add(infos);
				Log.d("UpdateConfig", "NeedUpdate " + infos[0] + " " + infos[1]);
			}
		} else{
			throw new Exception(" 更新内容为空");
		}
		return needUpdate;
	}

	private static String getStringFromUrl(String url) {
		return getStringFromUrl(url, "utf-8");
	}

	/**
	 * 获取URL的内容
	 * 
	 * @param url
	 * @param charSet
	 * @return
	 */
	private synchronized static String getStringFromUrl(String url, String charSet) {
		// 得到HttpGet对象
		String result = null;
		try {
			HttpGet request = new HttpGet(url);
			HttpResponse response = getClient.execute(request);
			// 判断请求是否成功
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 获得输入流
				InputStream in = response.getEntity().getContent();
				result = IOUtil.readStream(in, charSet);
				in.close();
				Log.v("UpdateConfig", "request " + url + " "
						+ response.getStatusLine().getReasonPhrase());
			} else {
				Log.v("UpdateConfig", "request " + url + " "
						+ response.getStatusLine().getReasonPhrase());
			}
		} catch (Exception e) {
			Log.v("UpdateConfig",
					e.getMessage() == null ? "get url fail" : e.getMessage());
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 获取最新版本号和下载地址
	 * 
	 * @return 返回最新版本、更新内容、最新地址，如果为null则表示请求失败
	 */
	public static String[] getNewestVersionInfos(){
		String content = getStringFromUrl(NEWEST_URL, "utf-8");
		if(content == null){
			return null;
		}
		// 以=-=-=-区分信息
		String[] versionInfos = content.split("=-=-=-");
		versionInfos[0] = versionInfos[0].trim();
		versionInfos[2] = versionInfos[2].trim();
		
		// 检测信息是否合法
		if(versionInfos.length < 3){
			return null;
		}
		if(!versionInfos[0].matches("(\\d+\\.)+\\d+")){
			return null;
		}
		if(!versionInfos[2].matches("http.*")){
			return null;
		}
		return versionInfos;
	}
	
	/**
	 * 开启一个线程检测版本更新
	 * @param activity
	 * @param arg1 参数为0如果没有更新则不提示，为1没有更新也需要提示
	 */
	public static void checkNewestVersion(final MainActivity activity, final int arg1){
		new Thread(new Runnable() {
			@Override
			public void run() {
				String[] newVersionInfos = getNewestVersionInfos();
				Message msg = new Message();
				msg.what = MainActivity.NEWVERSION;
				msg.obj = newVersionInfos;
				msg.arg1 = arg1;
				activity.mHandler.sendMessage(msg);
			}
		}).start();
	}
	
}
