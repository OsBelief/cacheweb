package com.yh.web.cache;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.util.Log;

public class UpdateTask {

	// 配置文件地址
	private static String configUrl = "http://122.49.34.20:18167/u/config.txt";

	private static long defaultSleepTime = 60000;
	private static long sleepTime = 60000;
	private static boolean runFlag = false;
	private static HttpClient getClient;
	private static Context context;

	/**
	 * 以默认参数初始化，每次延时60s
	 * 
	 * @param context
	 */
	public static void initShedule(Context context) {
		// 开始任务
		getClient = new DefaultHttpClient();
		UpdateTask.context = context;
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
	public static void initShedule(Context context, long sleepTime) {
		getClient = new DefaultHttpClient();
		UpdateTask.context = context;
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
						Log.d("UpdateTask", "Net or CPU is buzy, pass update");
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

	/**
	 * 执行更新配置操作
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	private static boolean updateConfig() throws ClientProtocolException,
			IOException {
		boolean res = false;
		List<String[]> needUpdate = getNeedUpdate();
		// 是否含有sleepTime
		boolean hasSleep = false;
		for (String[] nameUrl : needUpdate) {
			// 获取内容
			String content = getStringFromUrl(nameUrl[1]);
			if (content == null) {
				continue;
			}

			if (nameUrl[0].equals(CacheFilter.FILTER_NAME)) {
				// 更新成功后写入文件
				if (CacheFilter.initFilter(content)) {
					IOUtil.writeInternalFile(context, nameUrl[0],
							content.getBytes("utf-8"));
					Log.i("UpdateConfig", "update " + nameUrl[0] + " ok");
				} else {
					Log.i("UpdateConfig", "update " + nameUrl[0] + " fail");
				}
			} else if (nameUrl[0].equals(CachePolicy.POLICY_NAME)) {
				// 更新成功后写入文件
				if (CachePolicy.initPolicy(content)) {
					IOUtil.writeInternalFile(context, nameUrl[0],
							content.getBytes("utf-8"));
					Log.i("UpdateConfig", "update " + nameUrl[0] + " ok");
				} else {
					Log.i("UpdateConfig", "update " + nameUrl[0] + " fail");
				}
			} else if (nameUrl[0].equals(MIME.MIME_NAME)) {
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
			} else {
				Log.w("UpdateConfig", "not fount nameUrl " + nameUrl[0] + " "
						+ nameUrl[1]);
			}
		}
		if (!hasSleep) {
			sleepTime = defaultSleepTime;
		}

		return res;
	}

	/**
	 * 获取需要更新的信息
	 * 
	 * @return
	 */
	private static List<String[]> getNeedUpdate() {
		List<String[]> needUpdate = new ArrayList<String[]>();

		String config = getStringFromUrl(configUrl);
		if (config != null) {
			String[] lines = config.split("\n");
			for (String line : lines) {
				if (line.startsWith("#") || line.trim().equals("")) {
					continue;
				}
				String infos[] = line.split(" +");
				if (infos.length != 2) {
					continue;
				}
				needUpdate.add(infos);
				Log.d("UpdateConfig", "NeedUpdate " + infos[0] + " " + infos[1]);
			}
		}
		return needUpdate;
	}

	private static String getStringFromUrl(String url) {
		return getStringFromUrl(url, "gbk");
	}

	/**
	 * 获取URL的内容
	 * 
	 * @param url
	 * @param charSet
	 * @return
	 */
	private static String getStringFromUrl(String url, String charSet) {
		// 得到HttpGet对象
		String result = null;
		try {
			HttpGet request = new HttpGet(url);
			HttpResponse response = getClient.execute(request);
			// 判断请求是否成功
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 获得输入流
				InputStream in = response.getEntity().getContent();
				result = IOUtil.readStream(in, "gbk");
				Log.v("UpdateConfig", "request " + url + " "
						+ response.getStatusLine().getReasonPhrase());
			} else {
				Log.v("UpdateConfig", "request " + url + " "
						+ response.getStatusLine().getReasonPhrase());
			}
		} catch (Exception e) {
			Log.v("UpdateConfig", e.getMessage());
		}
		return result;
	}
}
