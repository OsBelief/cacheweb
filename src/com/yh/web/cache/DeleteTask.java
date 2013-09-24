package com.yh.web.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.yh.web.cache.db.CacheOrm;

/**
 * 计划任务
 * 
 * @author gudh
 * 
 *         执行定时删除过期数据等
 */
public class DeleteTask {

	private static CacheOrm orm = null;
	private static int everyDelCount = 20;
	private static long sleepTime = 60000;
	private static boolean runFlag = false;

	/**
	 * 以默认参数初始化，每次删除20个，每次延时60s
	 * 
	 * @param context
	 */
	public static void initShedule(Context context) {
		orm = new CacheOrm(context);
		// 开始任务
		startDeleteTask();
	}

	/**
	 * 初始化orm和定时任务
	 * 
	 * @param context
	 *            用于初始化数据库ORM
	 * @param everyDelCount
	 *            每次删除数量，至少1个
	 * @param sleepTime
	 *            单位ms，每次删除延时，至少1000ms
	 */
	public static void initShedule(Context context, int everyDelCount,
			long sleepTime) {
		orm = new CacheOrm(context);

		if (everyDelCount >= 1) {
			DeleteTask.everyDelCount = everyDelCount;
		}
		if (sleepTime >= 1000) {
			DeleteTask.sleepTime = sleepTime;
		}
		// 开始任务
		startDeleteTask();
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
				long start, end;
				// 记录删除次数
				int i = 0;
				while (runFlag) {
					// 如果网络繁忙则咱不进行删除操作
					if (!NetMonitor.isNetBuzy() && !StatMonitor.isCPUBuzy()) {
						start = System.currentTimeMillis();
						try {
							List<CacheObject> objs = DeleteTask
									.getExpireCache(everyDelCount);
							DeleteTask.deleteExpireCache(objs);
						} catch (Exception e) {
							Log.e("DeleteTask", e.getMessage());
						}
						end = System.currentTimeMillis();
						Log.i("DeleteTask", "delete cache use time : "
								+ (end - start));
					} else {
						Log.d("DeleteTask", "Net or CPU is buzy, pass delete expire");
					}

					// 没删除十次缓存获取空文件夹的存在并删除
					if (++i >= 10 && !NetMonitor.isNetBuzy()) {
						i = 0;

						start = System.currentTimeMillis();
						try {
							List<File> files = DeleteTask
									.scanEmptyFolders(CacheObject.rootPath);
							Log.i("DeleteFolder",
									"result "
											+ DeleteTask.deleteFolders(files));
						} catch (Exception e) {
							Log.e("DeleteTask", e.getMessage());
						}
						end = System.currentTimeMillis();
						Log.i("DeleteTask", "delete empty folder use time : "
								+ (end - start));
					}

					try {
						Thread.sleep(DeleteTask.sleepTime);
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
	public static boolean deleteExpireCache(List<CacheObject> objs) {
		int i = 0;
		for (CacheObject obj : objs) {
			File file = new File(obj.getFileName());
			// 文件存在
			if (file.exists()) {
				// 删除成功
				if (file.delete()) {
					if (orm.delete(obj)) {
						Log.d("DeleteExpire",
								obj.getFileName() + " " + obj.getUrl());
						i++;
					}
				}
			} else {
				// 文件不存在直接删除
				if (orm.delete(obj)) {
					Log.d("DeleteExpire",
							obj.getFileName() + " " + obj.getUrl());
					i++;
				}
			}
		}
		// 删除数量是否为objs的数量
		return i == objs.size();
	}
	
	/**
	 * 更加URL获取需要删除的缓存对象
	 * @param delUrls
	 * @return
	 */
	public static List<CacheObject> getNeedDeleteUrlCache(List<String> delUrlWheres){
		ArrayList<CacheObject> list = new ArrayList<CacheObject>();
		List<CacheObject> newList;
		for(String where : delUrlWheres){
			String sql = CacheOrm.baseQuery + where;
			newList = orm.query(sql);
			list.addAll(newList);
			Log.d("getNeedDeleteUrlCache", sql + " | this count : " + newList.size() + " all count :" + list.size());
		}
		return list;
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

		StringBuffer log = new StringBuffer();
		// 依次获取不同缓存策略的过期的信息
		for (String[] idbt : idBeforeTimes) {
			objs.addAll(orm.query(where, idbt,
					String.valueOf(count - objs.size())));
			log.append("id:").append(idbt[0]).append("  beforeTime:")
					.append(idbt[1]).append(" nowCount:").append(objs.size())
					.append(" | ");
			if (objs.size() >= count) {
				break;
			}
		}
		Log.d("GetExpireCache", log.toString());
		return objs;
	}

	/**
	 * 删除文件夹
	 * 
	 * @param files
	 * @return
	 */
	public static boolean deleteFolders(List<File> files) {
		int i = 0;
		for (File f : files) {
			if (f.exists()) {
				if (f.delete()) {
					i++;
					Log.d("DeleteFolder", f.getAbsolutePath());
				}
			}
		}
		return i == files.size();
	}

	/**
	 * 扫描指定路径下的所有空白文件夹
	 * 
	 * @param path
	 * @return
	 */
	public static List<File> scanEmptyFolders(String path) {
		List<File> emptys = new ArrayList<File>();
		// 算法实现
		File root = new File(path);
		// 存储为遍历的文件夹
		LinkedList<File> list = new LinkedList<File>();
		File files[] = root.listFiles();
		for (int i = 0; i < files.length; i++) {
			// System.out.println(files[i].getAbsolutePath());
			if (files[i].isDirectory()) {
				list.add(files[i]);
			}
		}

		File tmp;
		while (!list.isEmpty()) {
			tmp = list.removeLast();
			// System.out.println(tmp.getAbsolutePath());
			if (tmp.isDirectory()) {
				files = tmp.listFiles();
				if (files == null || files.length == 0) {
					emptys.add(tmp);
					continue;
				}
				for (int i = 0; i < files.length; i++) {
					// System.out.println(files[i].getAbsolutePath());
					if (files[i].isDirectory()) {
						list.add(files[i]);
					}
				}
			}
		}
		Log.d("GetEmptyFoler", "empty folder count : " + emptys.size());
		return emptys;
	}
}
