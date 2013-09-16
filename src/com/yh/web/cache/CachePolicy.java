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
 * @author gudh �������
 */
public class CachePolicy {

	private static String policyFileName = "policy.yaml";

	public final static int defaultPolicy = 0;

	// �洢���л������
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
			// String yamltxt = IOUtil.readStream(new FileInputStream("D:\\Android\\YichaWeb\\assets\\policy.yaml"));
			
			HashMap<String, Object> obj = (HashMap<String, Object>) yaml
					.load(yamltxt);
			List<HashMap<String, Object>> pols = (List<HashMap<String, Object>>) obj
					.get("cachePolicy");
			for (HashMap<String, Object> pol : pols) {
				Integer id = (Integer) pol.get("id");
				if (id != null) {
					// �½��������map
					CachePolicy pObj = new CachePolicy(id);
					policys.put(id, pObj);

					// ��ȡ�������Ϣ
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
	 * �������ͻ�ȡ�������
	 * 
	 * @param url
	 * @param type
	 * @param mime
	 * @return
	 */
	public static int getCachePolicy(String url, String type, String mime) {

		// ����Ĭ�ϲ���
		return defaultPolicy;
	}

	/**
	 * ���ݴ���ʱ�䡢��ǰʱ��ͻ�����ԣ��ж��Ƿ����
	 * 
	 * @param createTime
	 * @param now
	 * @param cachePolicy
	 * @return
	 */
	public static boolean isExpire(long createTime, long nowTime,
			int cachePolicy) {
		// �˴�ʵ���жϹ��ڴ���
		try {
			CachePolicy cp = policys.get(cachePolicy);

			Calendar createCal = Calendar.getInstance();
			createCal.setTimeInMillis(createTime);
			Calendar nowCal = Calendar.getInstance();
			nowCal.setTimeInMillis(nowTime);
			
			//Log.d("time", createCal + " " + nowCal);
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
			policy = ((ArrayList<String>) value).toArray(new String[]{});
		} else if (field.equals("month")) {
			month = (Integer) value;
		} else if (field.equals("week")) {
			this.week = (Integer) value;
		} else if (field.equals("day")) {
			this.day = (Integer) value;
		} else if (field.equals("time")) {
			this.time = (long)((Integer) value) * 1000;
		}
	}
}
