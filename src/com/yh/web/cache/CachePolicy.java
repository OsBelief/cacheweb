package com.yh.web.cache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import android.content.res.AssetManager;
import android.util.Log;
import android.util.SparseArray;

/**
 * @author gudh 缓存策略
 */
public class CachePolicy {

	private static String policyFileName = "mimes.txt";

	public final static int defaultPolicy = 0;

	// 存储所有缓存策略
	private static SparseArray<CachePolicy> policys = new SparseArray<CachePolicy>();

	private int id;
	private String[] policy;
	private int month;
	private int week;
	private int day;
	private long time;

	private CachePolicy(int id) {
		this.id = id;
	}

	@SuppressWarnings("unchecked")
	public static void initPolicy(AssetManager assets) {
		Yaml yaml = new Yaml();
		try {
			String yamltxt = IOUtil.readStream(assets.open(policyFileName))
					.trim();

			HashMap<String, Object> obj = (HashMap<String, Object>) yaml
					.load(yamltxt);
			List<HashMap<String, Object>> pols = (List<HashMap<String, Object>>) obj
					.get("cachePolicy");
			for (HashMap<String, Object> pol : pols) {
				Integer id = (Integer) pol.get("id");
				if (id != null) {
					// 新建对象放入map
					CachePolicy pObj = new CachePolicy(id);
					policys.put(id, pObj);

					// 获取对象的信息
					List<String> fields = (List<String>) pol.get("policy");
					pObj.set("policy", fields);
					
					for (String field : fields) {
						if (pol.containsKey(field)) {
							pObj.set(field, pol.get(field));
						}
					}
				}
			}
			Log.d("initPolicy", obj.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据类型获取缓存策略
	 * 
	 * @param url
	 * @param type
	 * @param mime
	 * @return
	 */
	public static int getCachePolicy(String url, String type, String mime) {

		// 返回默认策略
		return defaultPolicy;
	}

	/**
	 * 根据创建时间、当前时间和缓存策略，判断是否过期
	 * 
	 * @param createTime
	 * @param now
	 * @param cachePolicy
	 * @return
	 */
	public static boolean isExpire(long createTime, long nowTime,
			int cachePolicy) {
		// 此处实现判断过期代码
		CachePolicy cp = policys.get(cachePolicy);

		Calendar createCal = Calendar.getInstance();
		createCal.setTimeInMillis(createTime);
		Calendar nowCal = Calendar.getInstance();
		nowCal.setTimeInMillis(nowTime);

		for (String pol : cp.policy) {
			if (pol.equals("month")) {
				if (nowCal.get(Calendar.MONTH) - createCal.get(Calendar.MONTH) > cp.month) {
					return false;
				}
			} else if (pol.equals("week")) {
				if (nowCal.get(Calendar.WEDNESDAY)
						- createCal.get(Calendar.WEDNESDAY) > cp.week) {
					return false;
				}
			} else if (pol.equals("day")) {
				if (nowCal.get(Calendar.DAY_OF_MONTH)
						- createCal.get(Calendar.DAY_OF_MONTH) > cp.day) {
					return false;
				}
			} else if (pol.equals("time")) {
				if (nowTime - createTime > cp.time) {
					return false;
				}
			}
		}
		return true;
	}

	public int getId() {
		return id;
	}

	public String[] getPolicy() {
		return policy;
	}

	public int getMonth() {
		return month;
	}

	public int getWeek() {
		return week;
	}

	public int getDay() {
		return day;
	}

	public long getTime() {
		return time;
	}

	public void set(String field, Object value) {
		if (field.equals("id")) {
			id = (Integer) value;
		} else if (field.equals("policy")) {
			policy = (String[]) value;
		} else if (field.equals("month")) {
			month = (Integer) value;
		} else if (field.equals("week")) {
			this.week = (Integer) value;
		} else if (field.equals("day")) {
			this.day = (Integer) value;
		} else if (field.equals("time")) {
			this.time = (Long) value;
		}
	}
}
