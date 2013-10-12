package com.yh.web.cache;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import android.util.Log;

/**
 * CPU 使用率监控
 * 
 * @author gudh
 * 
 */
public class StatMonitor {

	// 系统CPU信息文件
	private final static String STAT_FILE = "/proc/stat";

	// 第一行
	private static final String CPULINE = "cpu ";

	// 上一次的CPU空闲和所有
	private static long[] cpuLast = { 0, 0 };
	// 锁，同时只有一个在统计
	private static boolean lock = false;
	// 是否要继续监控
	private static boolean isJudge = true;
	// 当前的cpu是否繁忙
	private static boolean cpuBuzy = false;
	// 上次时间
	private static long lastTime = 0;

	private static float cpuMax = 0.6f; // 最大CPU使用率
	private static long judgeSleep = 3000; // 3秒一次CPU监控

	/**
	 * 设置监控的参数，为-1则使用默认
	 * 
	 * @param cpuMax
	 * @param judgeSleep
	 */
	public static void setParameter(float cpuMax, long judgeSleep) {
		if (cpuMax != -1) {
			StatMonitor.cpuMax = cpuMax;
		}
		if (judgeSleep != -1) {
			StatMonitor.judgeSleep = judgeSleep;
		}
	}

	/**
	 * 获取当前CPU是否繁忙
	 * 
	 * @return
	 */
	public static boolean isCPUBuzy() {
		return cpuBuzy;
	}

	/**
	 * 初始化网络监控
	 */
	public static void startJudge() {
		// 启动线程定时监控
		isJudge = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (isJudge) {
					try {
						StatMonitor.judgeStatBuzy();

						// 延时
						Thread.sleep(StatMonitor.judgeSleep);
					} catch (Exception e) {
						Log.e("StatInfo", e.getMessage());
					}
				}
			}
		}).start();
	}

	/**
	 * 结束网络监控
	 */
	public static void stopJudge() {
		isJudge = false;
	}

	/**
	 * 判断网络是否繁忙
	 * 
	 * @return
	 */
	public static boolean judgeStatBuzy() {
		// 获取当前的网络信息
		long[] statInfo = getLastStatInfo();
		if (statInfo == null) {
			return isCPUBuzy();
		}

		float cpu = (1f - ((float) (statInfo[0] - cpuLast[0]))
				/ (statInfo[1] - cpuLast[1]));

		cpuBuzy = cpu > cpuMax;

		long now = System.currentTimeMillis();
		if (cpuBuzy) {
			Log.w("StatInfo",
					new StringBuffer().append("CPU Buzy | ").append(cpu)
							.append(" ").append(statInfo[0] - cpuLast[0])
							.append(" ").append(statInfo[1] - cpuLast[1])
							.append(" ").append(now - lastTime).toString());
		} else {
			Log.d("StatInfo", new StringBuffer().append("CPU Not Buzy | ")
					.append(cpu).append(" ").append(statInfo[0] - cpuLast[0])
					.append(" ").append(statInfo[1] - cpuLast[1]).append(" ")
					.append(now - lastTime).toString());
		}

		if (cpu > 0) {
			cpuLast[0] = statInfo[0];
			cpuLast[1] = statInfo[1];
		}
		lastTime = now;

		return cpuBuzy;
	}

	/**
	 * 获取当前(CPU空闲、占用、采集时间)
	 * 
	 * @return
	 */
	public static long[] getLastStatInfo() {
		// 获取锁
		if (lock) {
			Log.d("StatInfo", "Now is runing!");
			return null;
		}
		lock = true;

		long[] res = { 0, 0 };
		FileReader fstream = null;
		try {
			fstream = new FileReader(STAT_FILE);
		} catch (FileNotFoundException e) {
			Log.e("StatInfo", "Could not read " + STAT_FILE);
		}

		BufferedReader in = new BufferedReader(fstream, 500);
		String line;
		String[] netData;
		try {
			while ((line = in.readLine()) != null) {

				if (line.startsWith(CPULINE)) {
					netData = line.replace(CPULINE, "").trim().split(" +");

					long use = Long.parseLong(netData[3]);
					long all = 0;
					for (int i = 0; i < 7; i++) {
						all += Long.parseLong(netData[i]);
					}

					res[0] = use;
					res[1] = all;
					break;
				}
			}
			fstream.close();

			return res;
		} catch (IOException e) {
			Log.e("StatInfo", e.toString());
		} finally {
			lock = false;
		}
		return null;
	}
}
