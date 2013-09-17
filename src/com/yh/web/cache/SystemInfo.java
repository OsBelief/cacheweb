package com.yh.web.cache;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import android.util.Log;

public class SystemInfo {

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

	// 上次统计时间
	public static long lastTime = -1;

	// 标志当前网络是否繁忙
	public static boolean isNetBuzy = false;

	// 判断网络延时
	public static long judgeSleep = 2000;

	// 是否要继续监控网络
	public static boolean isJudge = true;

	/**
	 * 初始化系统监控
	 */
	public static void startJudge() {
		// 启动线程定时监控
		isJudge = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (isJudge) {
					try {
						SystemInfo.judgeNetBuzy();
						

						// 延时
						Thread.sleep(SystemInfo.judgeSleep);
					} catch (Exception e) {
						Log.e("NetInfo", e.getMessage());
					}
				}
			}
		}).start();
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

		if (netInfo[0] > 204800 * seconds) {
			// 每秒超过200k
			isNetBuzy = true;
		} else if (netInfo[1] > 400 * seconds) {
			// 每秒发包超过200
			isNetBuzy = true;
		} else if (netInfo[2] > 10 * seconds) {
			// 每秒丢包数大于10
			isNetBuzy = true;
		} else {
			isNetBuzy = false;
		}
		
		if(isNetBuzy){
			Log.w("NetInfo",  "BDP Now True | " + netInfo[0] + "  " + netInfo[1] + "  " + netInfo[2] + "  " + netInfo[3] + "  " + netInfo[4]);
		}else{
			Log.d("NetInfo",  "BDP Now False | " + netInfo[0] + "  " + netInfo[1] + "  " + netInfo[2] + "  " + netInfo[3] + "  " + netInfo[4]);
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
					//Log.d("NetInfo", line);
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
