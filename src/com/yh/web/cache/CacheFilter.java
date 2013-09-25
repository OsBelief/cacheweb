package com.yh.web.cache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * @author gudh
 * 
 *         配置文件
 */
public class CacheFilter {

	public final static String FILTER_NAME = "filter.yaml";
	public final static String CONFIG_NAME = "config.yaml";

	// 最大允许的URL长度
	public static int maxUrlLength = 200;
	// 不缓存的URL匹配
	public static List<String> disCacheUrlList;
	// URL地址替换
	public static List<HashMap<String, Object>> cacheUrlReplaceList;
	// 需要缓存URL的类型和对应的匹配正则
	public static LinkedHashMap<String, String> cacheTypeUrlMap;
	// 不缓存的类型
	public static HashSet<String> notCacheType;
	// 对指定host的不缓存
	public static List<HashMap<String, Object>> hostDisCacheTypeList;

	/**
	 * 初始化过滤规则
	 */
	public static void initFilter(Activity act) {

		try {
			// 先从外部读文件，如果没有，读取asset的配置
			InputStream in = IOUtil.readInternalFile(act, FILTER_NAME);
			if (in == null) {
				in = act.getAssets().open(FILTER_NAME);
				Log.i("initFilter", "init from asset");
			} else {
				Log.i("initFilter", "init from internal file");
			}
			String yamltxt = IOUtil.readStream(in).trim();
			initFilter(yamltxt);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static boolean initFilter(String yamltxt) {
		ArrayList<String> disCacheUrlListNew = new ArrayList<String>();
		ArrayList<HashMap<String, Object>> cacheUrlReplaceListNew = new ArrayList<HashMap<String, Object>>();
		LinkedHashMap<String, String> cacheTypeUrlMapNew = new LinkedHashMap<String, String>();
		HashSet<String> notCacheTypeNew = new HashSet<String>();
		List<HashMap<String, Object>> hostDisCacheTypeNew = new ArrayList<HashMap<String, Object>>();

		boolean res = false;
		Yaml yaml = new Yaml();
		try {
			Log.d("initFilter", yamltxt);
			LinkedHashMap<String, Object> obj = (LinkedHashMap<String, Object>) yaml
					.load(yamltxt);
			if (obj.get("maxUrlLength") != null) {
				maxUrlLength = (Integer) obj.get("maxUrlLength");
			}
			if (obj.get("disCacheUrl") instanceof List) {
				disCacheUrlListNew
						.addAll((List<String>) obj.get("disCacheUrl"));
				// 切换
				disCacheUrlList = disCacheUrlListNew;
				disCacheUrlListNew = null;
			}
			if (obj.get("cacheUrlReplace") instanceof List) {
				cacheUrlReplaceListNew
						.addAll((List<HashMap<String, Object>>) obj
								.get("cacheUrlReplace"));
				// 切换
				cacheUrlReplaceList = cacheUrlReplaceListNew;
				cacheUrlReplaceListNew = null;
			}
			if (obj.get("cacheTypeUrl") instanceof Map) {
				cacheTypeUrlMapNew.putAll((LinkedHashMap<String, String>) obj
						.get("cacheTypeUrl"));
				// 切换
				cacheTypeUrlMap = cacheTypeUrlMapNew;
				cacheTypeUrlMapNew = null;
			}
			if (obj.get("notCacheType") instanceof List) {
				notCacheTypeNew.addAll((List<String>) obj.get("notCacheType"));
				// 切换
				notCacheType = notCacheTypeNew;
				notCacheTypeNew = null;
			}
			if (obj.get("hostDisCacheType") instanceof List) {
				hostDisCacheTypeNew.addAll((List<HashMap<String, Object>>) obj
						.get("hostDisCacheType"));
				// 切换
				hostDisCacheTypeList = hostDisCacheTypeNew;
				hostDisCacheTypeNew = null;
			}
			res = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * 初始化配置文件
	 */
	public static void initConfig(AssetManager assets) {
		Yaml yaml = new Yaml();
		try {
			String yamltxt = IOUtil.readStream(assets.open(CONFIG_NAME)).trim();
			@SuppressWarnings("unchecked")
			LinkedHashMap<String, Object> obj = (LinkedHashMap<String, Object>) yaml
					.load(yamltxt);

			Log.d("initConfig", obj.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 判断是否能通过HOST的过滤
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean passHostFilter(CacheObject obj) {
		boolean res = true;
		out: for (HashMap<String, Object> hostDisCacheType : hostDisCacheTypeList) {
			if (obj.getHost().equals(hostDisCacheType.get("host"))) {
				for (String type : (ArrayList<String>) hostDisCacheType
						.get("types")) {
					if (obj.getType().equals(type)) {
						res = false;
						break out;
					}
				}
			}
		}
		return res;
	}
}
