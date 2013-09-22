package com.yh.web.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.yh.web.cache.db.CacheOrm;

/**
 * 计划任务
 * 
 * @author gudh
 * 
 * 执行定时删除过期数据等
 */
public class ScheduleTask {

	private static CacheOrm orm = null;
	private static int everyDelCount = 10;
	private static long sleepTime = 60000;
	private static boolean runFlag = false;

	/**
	 * 初始化orm和定时任务
	 * 
	 * @param context
	 */
	public static void initShedule(Context context, int everyDelCount,
			long sleepTime) {
		orm = new CacheOrm(context);
		ScheduleTask.everyDelCount = everyDelCount;
		ScheduleTask.sleepTime = sleepTime;
	}

	/**
	 * 启动线程运行删除任务
	 * 
	 * @param everyDelCount
	 * @param sleepTime
	 * @return
	 */
	public static void startDeleteTask() {
		runFlag = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (runFlag) {
					try {
						List<CacheObject> objs = ScheduleTask
								.getExpireCache(everyDelCount);
						ScheduleTask.deleteExpireCache(objs);
					} catch (Exception e) {
						Log.e("DeleteTask", e.getMessage());
					}

					try {
						Thread.sleep(ScheduleTask.sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * 停止删除任务
	 */
	public static void stopDeleteTask() {
		runFlag = false;
	}

	/**
	 * 删除过期的缓存信息
	 */
	private static boolean deleteExpireCache(List<CacheObject> objs) {
		int i = 0;
		for (CacheObject obj : objs) {
			File file = new File(obj.getFileName());
			// 文件存在
			if (file.exists()) {
				// 删除成功
				if (file.delete()) {
					if (orm.delete(obj)) {
						i++;
					}
				}
			} else {
				// 文件不存在直接删除
				if (orm.delete(obj)) {
					i++;
				}
			}
		}
		// 删除数量是否为objs的数量
		return i == objs.size();
	}

	/**
	 * 获取指定数量的过期缓存对象
	 * 
	 * @param count
	 * @return
	 */
	private static List<CacheObject> getExpireCache(int count) {
		List<CacheObject> objs = new ArrayList<CacheObject>(count);
		String where = "cachePolicy = ? and createTime < ?";

		List<String[]> idBeforeTimes = CachePolicy.getAllCacheIdBeforeTimes();

		// 依次获取不同缓存策略的过期的信息
		for (String[] idbt : idBeforeTimes) {
			objs.addAll(orm.query(where, idbt,
					String.valueOf(count - objs.size())));
			if (objs.size() >= count) {
				break;
			}
		}
		return objs;
	}
}
