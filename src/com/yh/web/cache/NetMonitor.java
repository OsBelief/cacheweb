package com.yh.web.cache;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import android.util.Log;

public class NetMonitor {

	// ϵͳ�����ļ�
	private final static String DEV_FILE = "/proc/self/net/dev";

	// eth����̫����Ϣ tiwlan0 �� Wifi rmnet0 �� GPRS
	private static final String GPRSLINE = "rmnet0";
	private static final String ETHLINE = "  eth0";
	// tiwlan0�� 2.1Ϊwlan0�� ����
	private static final String WIFILINE = " wlan0";

	// ��һ�ε����BytePacketDrop
	private static long[] bpdLast = { 0, 0, 0 };
	// ���ڵ���� BytePacketDrop
	private static long[] bpdNow = { 0, 0, 0 };
	// ����ͬʱֻ��һ����ͳ��
	private static boolean lock = false;
	// �Ƿ�Ҫ�����������
	private static boolean isJudge = true;
	// �ϴ�ͳ��ʱ��
	private static long lastTime = -1;
	// ��־��ǰ�����Ƿ�æ
	private static boolean isNetBuzy = false;

	private static long byteMax = 102400; // �������(byte/s)
	private static long packetMax = 200; // ��󷢰� (p/s)
	private static long dropMax = 10; // ��󶪰�(p/s)
	private static long judgeSleep = 5000; // �ж�����(ms)

	/**
	 * ���ü�صĲ���������Ϊ-1�򲻸���
	 * 
	 * @param byteMax
	 *            �������(byte/s)��Ĭ��102400(100k)
	 * @param packetMax
	 *            ��󷢰� (p/s)��Ĭ��200
	 * @param dropMax
	 *            ��󶪰�(p/s)��Ĭ��10
	 * @param judgeSleep
	 *            �ж�����(ms)��Ĭ��5000(5s)
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
	 * ��ȡ��ǰ�����Ƿ�æµ״̬
	 * 
	 * @return
	 */
	public static boolean isNetBuzy() {
		return isNetBuzy;
	}

	/**
	 * ��ʼ��������
	 */
	public static void startJudge() {
		// �����̶߳�ʱ���
		isJudge = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (isJudge) {
					try {
						NetMonitor.judgeNetBuzy();

						// ��ʱ
						Thread.sleep(NetMonitor.judgeSleep);
					} catch (Exception e) {
						Log.e("NetInfo", e.getMessage());
					}
				}
			}
		}).start();
	}

	/**
	 * ����������
	 */
	public static void stopJudge() {
		isJudge = false;
	}

	/**
	 * �ж������Ƿ�æ
	 * 
	 * @return
	 */
	public static boolean judgeNetBuzy() {
		// ��ȡ��ǰ��������Ϣ
		long[] netInfo = getLastNetTraffic();
		if (netInfo == null) {
			return isNetBuzy;
		}
		long seconds = netInfo[4] / 1000;

		if (netInfo[0] > byteMax * seconds) {
			// ÿ�볬��200k
			isNetBuzy = true;
		} else if (netInfo[1] > packetMax * seconds) {
			// ÿ�뷢������200
			isNetBuzy = true;
		} else if (netInfo[2] > dropMax * seconds) {
			// ÿ�붪��������10
			isNetBuzy = true;
		} else {
			isNetBuzy = false;
		}

		if (isNetBuzy) {
			Log.w("NetInfo", "BDP Now True | " + netInfo[0] + "  " + netInfo[1]
					+ "  " + netInfo[2] + "  " + netInfo[3] + "  " + netInfo[4]);
		} else {
			Log.d("NetInfo", "BDP Now False | " + netInfo[0] + "  "
					+ netInfo[1] + "  " + netInfo[2] + "  " + netInfo[3] + "  "
					+ netInfo[4]);
		}

		return isNetBuzy;
	}

	/**
	 * ��ȡ��ȥ��һ��ʱ���ڣ�ͳ�Ƶ��������ֽڣ�����������ͳ����������ͳ��ʱ�䣩��Ϣ��
	 * 
	 * @return
	 */
	public static long[] getLastNetTraffic() {
		// ��ȡ��
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
