package com.yh.web.cache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import android.content.res.AssetManager;
import android.util.Log;

/**
 * @author gudh
 * 
 *         �����ļ�
 */
public class Config {

	public final static String filterName = "filter.yaml";
	public final static String configName = "config.yaml";

	// ��������URL����
	public static int maxUrlLength = 200;
	// �������URLƥ��
	public static List<String> disCacheUrlList;
	// URL��ַ�滻
	public static List<HashMap<String, Object>> cacheUrlReplaceList;
	// ��Ҫ����URL�����ͺͶ�Ӧ��ƥ������
	public static LinkedHashMap<String, String> cacheTypeUrlMap;
	// �����������
	public static HashSet<String> notCacheType;

	@SuppressWarnings("unchecked")
	/**
	 * ��ʼ�����˹���
	 */
	public static void initFilter(AssetManager assets) {
		if (disCacheUrlList == null) {
			disCacheUrlList = new ArrayList<String>();
		}
		if (cacheUrlReplaceList == null) {
			cacheUrlReplaceList = new ArrayList<HashMap<String, Object>>();
		}
		if (cacheTypeUrlMap == null) {
			cacheTypeUrlMap = new LinkedHashMap<String, String>();
		}
		if (notCacheType == null) {
			notCacheType = new HashSet<String>();
		}
		Yaml yaml = new Yaml();
		try {
			String yamltxt = IOUtil.readStream(assets.open(filterName)).trim();
			Log.d("initFilter", yamltxt);
			LinkedHashMap<String, Object> obj = (LinkedHashMap<String, Object>) yaml
					.load(yamltxt);
			if (obj.get("maxUrlLength") != null) {
				maxUrlLength = (Integer) obj.get("maxUrlLength");
			}
			if (obj.get("disCacheUrl") instanceof List) {
				disCacheUrlList.addAll((List<String>) obj.get("disCacheUrl"));
			}
			if (obj.get("cacheUrlReplace") instanceof List) {
				cacheUrlReplaceList.addAll((List<HashMap<String, Object>>) obj
						.get("cacheUrlReplace"));
			}
			if (obj.get("cacheTypeUrl") instanceof Map) {
				cacheTypeUrlMap.putAll((LinkedHashMap<String, String>) obj
						.get("cacheTypeUrl"));
			}
			if (obj.get("notCacheType") instanceof List) {
				notCacheType.addAll((List<String>) obj.get("notCacheType"));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ʼ�������ļ�
	 */
	public static void initConfig(AssetManager assets) {
		Yaml yaml = new Yaml();
		try {
			String yamltxt = IOUtil.readStream(assets.open(configName)).trim();
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
}
