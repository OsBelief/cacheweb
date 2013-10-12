package com.yh.web.cache;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import android.util.Log;

/**
 * 网络繁忙监控
 * @author gudh
 *
 */
public class NetMonitor {

	// 系统流量文件
	private final static String DEV_FILE = "/proc/self/net/dev";

	// eth是以太网信息 tiwlan0 是 Wifi rmnet0 是 GPRS
	private static final String GPRSLINE = "rmnet0";
	private static final String ETHLINE = "  eth0";
	// tiwlan0， 2.1为wlan0， 兼容
	private static final String WIFILINE = " wlan0";

	// 上一次的情况BytePacketDrop
	private static long[] bpdLast = { 0, 0, 0 };
	// 现在的情况 BytePacketDrop
	private static long[] bpdNow = { 0, 0, 0 };
	// 锁，同时只有一个在统计
	private static boolean lock = false;
	// 是否要继续监控网络
	private static boolean isJudge = true;
	// 上次统计时间
	private static long lastTime = -1;
	// 标志当前网络是否繁忙
	private static boolean isNetBuzy = false;

	private static long byteMax = 102400; // 最大网速(byte/s)
	private static long packetMax = 200; // 最大发包 (p/s)
	private static long dropMax = 10; // 最大丢包(p/s)
	private static long judgeSleep = 5000; // 判断周期(ms)

	/**
	 * 设置监控的参数，参数为-1则不更改
	 * 
	 * @param byteMax
	 *            最大网速(byte/s)，默认102400(100k)
	 * @param packetMax
	 *            最大发包 (p/s)，默认200
	 * @param dropMax
	 *            最大丢包(p/s)，默认10
	 * @param judgeSleep
	 *            判断周期(ms)，默认5000(5s)
	 */
	public static void setParameter(long byteMax, long packetMax, long dropMax,
			long judgeSleep) {
		if (byteMax != -1) {
			NetMonitor.byteMax = byteMax;
		}
		if (packetMax != -1) {
			NetMonitor.packetMax = packetMax;
		}
		if (dropMax != -1) {
			NetMonitor.dropMax = dropMax;
		}
		if (judgeSleep != -1) {
			NetMonitor.judgeSleep = judgeSleep;
		}
	}

	/**
	 * 获取当前网络是否忙碌状态
	 * 
	 * @return
	 */
	public static boolean isNetBuzy() {
		return isNetBuzy;
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
						NetMonitor.judgeNetBuzy();

						// 延时
						Thread.sleep(NetMonitor.judgeSleep);
					} catch (Exception e) {
						Log.e("NetInfo", e.getMessage());
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
	public static boolean judgeNetBuzy() {
		// 获取当前的网络信息
		long[] netInfo = getLastNetTraffic();
		if (netInfo == null) {
			return isNetBuzy;
		}
		long seconds = netInfo[4] / 1000;

		if (netInfo[0] > byteMax * seconds) {
			// 每秒超过200k
			isNetBuzy = true;
		} else if (netInfo[1] > packetMax * seconds) {
			// 每秒发包超过200
			isNetBuzy = true;
		} else if (netInfo[2] > dropMax * seconds) {
			// 每秒丢包数大于10
			isNetBuzy = true;
		} else {
			isNetBuzy = false;
		}

		if (isNetBuzy) {
			Log.w("NetInfo", "Net Buzy | " + netInfo[0] + "  " + netInfo[1]
					+ "  " + netInfo[2] + "  " + netInfo[3] + "  " + netInfo[4]);
		} else {
			Log.d("NetInfo", "Net Not Buzy | " + netInfo[0] + "  " + netInfo[1]
					+ "  " + netInfo[2] + "  " + netInfo[3] + "  " + netInfo[4]);
		}

		return isNetBuzy;
	}

	/**
	 * 获取过去的一段时间内，统计的流量（字节，包，丢包，统计网卡数，统计时间）信息。
	 * 
	 * @return
	 */
	public static long[] getLastNetTraffic() {
		// 获取锁
		if (lock) {
			Log.d("NetInfo", "Now is runing!");
			return null;
		}
		lock = true;

		long[] res = { 0, 0, 0, 0, 0 };
		FileReader fstream = null;
		try {
			fstream = new FileReader(DEV_FILE);
		} catch (FileNotFoundException e) {
			Log.e("NetInfo", "Could not read " + DEV_FILE);
		}

		BufferedReader in = new BufferedReader(fstream, 500);
		String line;
		String[] segs;
		String[] netData;
		try {
			while ((line = in.readLine()) != null) {
				segs = line.trim().split(":");

				if (line.startsWith(ETHLINE) || line.startsWith(GPRSLINE)
						|| line.startsWith(WIFILINE)) {
					netData = segs[1].trim().split(" +");

					bpdNow[0] += Long.parseLong(netData[0])
							+ Long.parseLong(netData[8]);
					bpdNow[1] += Long.parseLong(netData[1])
							+ Long.parseLong(netData[9]);
					bpdNow[2] += Long.parseLong(netData[3])
							+ Long.parseLong(netData[11]);
					// Log.d("NetInfo", line);
					res[3] = res[3] + 1;
				}
			}
			long now = System.currentTimeMillis();
			fstream.close();

			for (int i = 0; i < 3; i++) {
				if (bpdNow[i] > i) {
					res[i] = bpdNow[i] - bpdLast[i];
				}
			}
			if (lastTime == -1) {
				res[4] = 1;
			} else {
				res[4] = now - lastTime;
			}

			lastTime = now;
			bpdLast = bpdNow;
			bpdNow = new long[] { 0, 0, 0 };

			return res;
		} catch (IOException e) {
			Log.e("NetInfo", e.toString());
		} finally {
			lock = false;
		}
		return null;
	}
}
