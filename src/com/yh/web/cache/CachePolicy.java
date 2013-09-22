package com.yh.web.cache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import android.content.res.AssetManager;
import android.util.Log;
import android.util.SparseArray;

/**
 * @author gudh
 * 
 *         缓存策略
 */
public class CachePolicy {

	private static String policyFileName = "policy.yaml";

	public final static int defaultPolicy = 0;

	// 存储所有的缓存匹配规则
	private static List<CacheMatch> cacheMatchs = new ArrayList<CacheMatch>();

	// 缓存策略的ID集
	private static List<Integer> cacheIds = new ArrayList<Integer>();

	// 存储所有缓存策略
	private static SparseArray<CachePolicy> cachePolicys = new SparseArray<CachePolicy>();

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
			Log.d("initPolicy", yamltxt);

			HashMap<String, Object> obj = (HashMap<String, Object>) yaml
					.load(yamltxt);
			// 解析cachePolicy
			List<HashMap<String, Object>> pols = (List<HashMap<String, Object>>) obj
					.get("cachePolicy");
			for (HashMap<String, Object> pol : pols) {
				Integer id = (Integer) pol.get("id");
				if (id != null) {
					CachePolicy pObj = new CachePolicy(id);
					cachePolicys.put(id, pObj);
					cacheIds.add(id);

					// 获取对象的信息
					List<String> fields = (List<String>) pol.get("policy");
					pObj.set("policy", fields);

					// 仅对给定属性进行设置
					for (String field : fields) {
						if (pol.containsKey(field)) {
							pObj.set(field, pol.get(field));
						}
					}
				}
			}
			// 解析cacheMatch
			List<HashMap<String, Object>> mats = (List<HashMap<String, Object>>) obj
					.get("cacheMatch");
			for (HashMap<String, Object> mat : mats) {
				Integer id = (Integer) mat.get("id");
				if (id != null) {
					// 新建对象放入map，CacheMatch三个匹配属性按顺序只取一个
					CacheMatch mObj = new CacheMatch(id);
					cacheMatchs.add(mObj);
					if (mat.containsKey("type")) {
						mObj.type = (String) mat.get("type");
					} else if (mat.containsKey("mime")) {
						mObj.mime = (String) mat.get("mime");
					} else if (mat.containsKey("url")) {
						mObj.url = (String) mat.get("url");
					}
				}
			}
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
		// 按顺序扫描，是否符合缓存策略
		for (CacheMatch mat : cacheMatchs) {
			if (mat.type != null && type.matches(mat.type)) {
				System.out.println(url + " cachePolicy " + mat.id);
				return mat.id;
			} else if (mat.mime != null && mime.matches(mat.mime)) {
				System.out.println(url + " cachePolicy " + mat.id);
				return mat.id;
			} else if (mat.url != null && url.matches(mat.url)) {
				System.out.println(url + " cachePolicy " + mat.id);
				return mat.id;
			}
		}
		System.out.println(url + " cachePolicy default");
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
		try {
			CachePolicy cp = cachePolicys.get(cachePolicy);
			Log.d("Expire", cachePolicy + " " + createTime + " " + nowTime
					+ " " + (nowTime - createTime) + " " + cp.time + " "
					+ cp.id);
			// 如果只有时间的话就不用Calendar了
			if (cp.policy.length == 1 && cp.policy[0].equals("time")) {
				return (nowTime - createTime > cp.time);
			}

			Calendar createCal = Calendar.getInstance();
			createCal.setTimeInMillis(createTime);
			Calendar nowCal = Calendar.getInstance();
			nowCal.setTimeInMillis(nowTime);

			// Log.d("time", createCal + " " + nowCal);
			for (String pol : cp.policy) {
				if (pol.equals("month")) {
					if (nowCal.get(Calendar.MONTH)
							- createCal.get(Calendar.MONTH) > cp.month) {
						return true;
					}
				} else if (pol.equals("week")) {
					if (nowCal.get(Calendar.WEDNESDAY)
							- createCal.get(Calendar.WEDNESDAY) > cp.week) {
						return true;
					}
				} else if (pol.equals("day")) {
					if (nowCal.get(Calendar.DAY_OF_MONTH)
							- createCal.get(Calendar.DAY_OF_MONTH) > cp.day) {
						return true;
					}
				} else if (pol.equals("time")) {
					if (nowTime - createTime > cp.time) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 获取所有缓存策略的id和该策略允许的最早的创建时间集合
	 * 
	 * @return
	 */
	public static List<String[]> getAllCacheIdBeforeTimes() {
		// 现在时间
		long now = System.currentTimeMillis() / 1000;

		// id和允许的最早创建时间
		List<String[]> idTimeBefores = new ArrayList<String[]>(cacheIds.size());

		// 循环获取每个缓存策略的最早允许创建时间
		for (int i = 0; i < cacheIds.size(); i++) {
			Integer id = cacheIds.get(i);
			String[] idtb = new String[2];
			idtb[0] = String.valueOf(id);

			long timeBefore = cachePolicys.get(id).getTime();
			// 如果time为0则为了效率不判断，直接保留，后期可加上
			if (timeBefore > 0) {
				timeBefore = now - timeBefore;
				idtb[1] = String.valueOf(timeBefore);

				idTimeBefores.add(idtb);
			}
		}
		return idTimeBefores;
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

	@SuppressWarnings("unchecked")
	public void set(String field, Object value) {
		if (field.equals("id")) {
			id = (Integer) value;
		} else if (field.equals("policy")) {
			policy = ((ArrayList<String>) value).toArray(new String[] {});
		} else if (field.equals("month")) {
			month = (Integer) value;
		} else if (field.equals("week")) {
			this.week = (Integer) value;
		} else if (field.equals("day")) {
			this.day = (Integer) value;
		} else if (field.equals("time")) {
			this.time = (long) ((Integer) value) * 1000;
		}
	}

	/**
	 * 存储缓存匹配规则，type,mime,url按顺序只取一个。如果有type则不要mime，否则有mime不要url
	 * 
	 * @author gudh
	 * 
	 */
	static class CacheMatch {
		int id;
		String type;
		String url;
		String mime;

		public CacheMatch(int id) {
			this.id = id;
		}
	}
}
