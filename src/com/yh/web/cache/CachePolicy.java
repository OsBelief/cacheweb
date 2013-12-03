package com.yh.web.cache;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import android.app.Activity;
import android.util.Log;
import android.util.SparseArray;

/**
 * @author gudh
 * 
 *         缓存策略
 */
public class CachePolicy {

	public final static String POLICY_NAME = "policy.yaml";

	public final static int defaultPolicy = 0;

	// 存储所有的缓存匹配规则
	private static List<CacheMatch> cacheMatchs;

	// 缓存策略的ID集
	private static List<Integer> cacheIds;

	// 存储所有缓存策略
	private static SparseArray<CachePolicy> cachePolicys;

	private int id;
	private String[] policy;
	private int month;
	private int week;
	private int day;
	private long time;

	private CachePolicy(int id) {
		this.id = id;
	}

	public static void initPolicy(Activity act) {
		try {
			// 先从外部读文件，如果没有，读取asset的配置
			InputStream in = IOUtil.readInternalFile(act, POLICY_NAME);
			if (in == null) {
				in = act.getAssets().open(POLICY_NAME);
				Log.i("initPolicy", "init from asset");
			} else{
				Log.i("initPolicy", "init from internal file");
			}
			String yamltxt = IOUtil.readStream(in).trim();
			initPolicy(yamltxt);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static boolean initPolicy(String yamltxt) {
		ArrayList<CacheMatch> cacheMatchsNew = new ArrayList<CacheMatch>();
		ArrayList<Integer> cacheIdsNew = new ArrayList<Integer>();
		SparseArray<CachePolicy> cachePolicysNew = new SparseArray<CachePolicy>();

		boolean res = false;
		Yaml yaml = new Yaml();
		try {
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
					cachePolicysNew.put(id, pObj);
					cacheIdsNew.add(id);

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
			if(cacheIdsNew.size() > 0){
				// 切换
				cachePolicys = cachePolicysNew;
				cachePolicysNew = null;
				cacheIds = cacheIdsNew;
				cacheIdsNew = null;
			}
			// 解析cacheMatch
			List<HashMap<String, Object>> mats = (List<HashMap<String, Object>>) obj
					.get("cacheMatch");
			for (HashMap<String, Object> mat : mats) {
				Integer id = (Integer) mat.get("id");
				if (id != null) {
					// 新建对象放入map，CacheMatch三个匹配属性按顺序只取一个
					CacheMatch mObj = new CacheMatch(id);
					cacheMatchsNew.add(mObj);
					if (mat.containsKey("type")) {
						mObj.type = (String) mat.get("type");
					} else if (mat.containsKey("mime")) {
						mObj.mime = (String) mat.get("mime");
					} else if (mat.containsKey("url")) {
						mObj.url = (String) mat.get("url");
					}
				}
			}
			if(cacheMatchsNew.size() > 0){
				// 切换
				cacheMatchs = cacheMatchsNew;
				cacheMatchsNew = null;
			}
			res = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
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
		boolean expire = false;
		try {
			CachePolicy cp = cachePolicys.get(cachePolicy);
			Log.d("Expire", cachePolicy + " " + createTime + " " + nowTime
					+ " " + (nowTime - createTime) + " " + cp.time + " "
					+ cp.id);

			// 获取是否过期
			expire = judgeExpire(createTime, nowTime, cp);

		} catch (Exception e) {
			Log.e("isExpire", e.getMessage());
			expire = false;
		}
		return expire;
	}

	/**
	 * 获取所有缓存策略的id和该策略允许的最早的创建时间集合
	 * 
	 * @return
	 */
	public static List<String[]> getAllCacheIdBeforeTimes() {
		// 现在时间
		long now = System.currentTimeMillis();

		// id和允许的最早创建时间
		List<String[]> idTimeBefores = new ArrayList<String[]>(cacheIds.size());

		// 循环获取每个缓存策略的最早允许创建时间
		for (int i = 0; i < cacheIds.size(); i++) {
			Integer id = cacheIds.get(i);
			String[] idtb = new String[2];
			idtb[0] = String.valueOf(id);

			CachePolicy cp = cachePolicys.get(id);
			// 获取当前id的最早未过期时间
			long timeBefore = getBeforeTime(now, cp);

			if (timeBefore > 0) {
				idtb[1] = String.valueOf(timeBefore);
				idTimeBefores.add(idtb);
			}
		}
		return idTimeBefores;
	}

	private static boolean judgeExpire(long createTime, long nowTime,
			CachePolicy cp) {
		// 如果只有时间的话就不用Calendar了
		if (cp.policy.length == 1 && cp.policy[0].equals("time")) {
			return (nowTime - createTime > cp.time);
		} else if(cp.policy.length == 0){
			return false;
		}

		Calendar createCal = Calendar.getInstance();
		createCal.setTimeInMillis(createTime);
		Calendar nowCal = Calendar.getInstance();
		nowCal.setTimeInMillis(nowTime);

		// Log.d("time", createCal + " " + nowCal);
		for (String pol : cp.policy) {
			if (pol.equals("month")) {
				if (nowCal.get(Calendar.MONTH) - createCal.get(Calendar.MONTH) > cp.month) {
					return true;
				}
			} else if (pol.equals("week")) {
				if (nowCal.get(Calendar.WEEK_OF_YEAR)
						- createCal.get(Calendar.WEDNESDAY) > cp.week) {
					return true;
				}
			} else if (pol.equals("day")) {
				if (nowCal.get(Calendar.DAY_OF_YEAR)
						- createCal.get(Calendar.DAY_OF_MONTH) > cp.day) {
					return true;
				}
			} else if (pol.equals("time")) {
				if (nowTime - createTime > cp.time) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 根据当前时间和规则获取最早未过期时间
	 * 
	 * @param now
	 * @param cp
	 * @return
	 */
	private static long getBeforeTime(long now, CachePolicy cp) {
		long timeBefore = now;
		if (cp.policy.length == 1 && cp.policy[0].equals("time")) {
			// 只有时间的话就不用Calendar对象了
			timeBefore = now - cp.getTime();
		} else if(cp.policy.length == 0){
			return -1;
		} else {
			// 根据日历判断月星期日
			long tempTimeBefore = 0;
			for (String pol : cp.policy) {
				if (pol.equals("month")) {
					Calendar nowCal = Calendar.getInstance();
					nowCal.setTimeInMillis(now);
					nowCal.add(Calendar.MONTH, 0 - cp.getMonth());
					tempTimeBefore = nowCal.getTimeInMillis();
					if (timeBefore > tempTimeBefore) {
						timeBefore = tempTimeBefore;
					}
				} else if (pol.equals("week")) {
					Calendar nowCal = Calendar.getInstance();
					nowCal.setTimeInMillis(now);
					nowCal.add(Calendar.WEEK_OF_YEAR, 0 - cp.getMonth());
					tempTimeBefore = nowCal.getTimeInMillis();
					if (timeBefore > tempTimeBefore) {
						timeBefore = tempTimeBefore;
					}
				} else if (pol.equals("day")) {
					Calendar nowCal = Calendar.getInstance();
					nowCal.setTimeInMillis(now);
					nowCal.add(Calendar.DAY_OF_YEAR, 0 - cp.getMonth());
					tempTimeBefore = nowCal.getTimeInMillis();
					if (timeBefore > tempTimeBefore) {
						timeBefore = tempTimeBefore;
					}
				} else if (pol.equals("time")) {
					tempTimeBefore = now - cp.getTime();
					if (timeBefore > tempTimeBefore) {
						timeBefore = tempTimeBefore;
					}
				}
			}
		}
		return timeBefore;
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
		} else if (field.equals("time")) {
			if(value instanceof Integer){
				this.time = ((Integer)value).longValue() * 1000; 
			} else if(value instanceof Long){
				this.time = (Long)value * 1000; 
			}
		} else if (field.equals("month")) {
			month = (Integer) value;
		} else if (field.equals("week")) {
			this.week = (Integer) value;
		} else if (field.equals("day")) {
			this.day = (Integer) value;
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
