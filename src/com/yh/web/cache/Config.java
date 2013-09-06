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

/**
 * @author gudh
 * 
 *         配置文件
 */
public class Config {

	public final static String filterName = "filter.yaml";
	public final static String configName = "config.yaml";
	
	// 最大允许的URL长度
	public static int maxUrlLength = 200;
	// 不缓存的URL匹配
	public static List<String> disCacheUrlList;
	// URL地址替换
	public static List<HashMap<String, Object>> cacheUrlReplaceList;
	// 需要缓存URL的类型和对应的匹配正则
	public static LinkedHashMap<String, String> cacheTypeUrlMap;
	// 判断是文件的类型
	public static HashSet<String> fileTypeSet;

	@SuppressWarnings("unchecked")
	/**
	 * 初始化过滤规则
	 */
	public static void initFilter(AssetManager assets) {
		if (disCacheUrlList == null) {
			disCacheUrlList = new ArrayList<String>();
		}
		if (cacheUrlReplaceList == null) {
			cacheUrlReplaceList = new ArrayList<HashMap<String,Object>>();
		}
		if (cacheTypeUrlMap == null) {
			cacheTypeUrlMap = new LinkedHashMap<String, String>();
		}
		if (fileTypeSet == null) {
			fileTypeSet = new HashSet<String>();
		}
		Yaml yaml = new Yaml();
		try {
			String yamltxt = IOUtil.readStream(assets.open(filterName))
					.trim();
			System.out.println(yamltxt);
			LinkedHashMap<String, Object> obj = (LinkedHashMap<String, Object>) yaml
					.load(yamltxt);
			if(obj.get("maxUrlLength") != null){
				maxUrlLength = (Integer)obj.get("maxUrlLength");
			}
			if (obj.get("disCacheUrl") instanceof List) {
				disCacheUrlList.addAll((List<String>) obj.get("disCacheUrl"));
			}
			if (obj.get("cacheUrlReplace") instanceof List) {
				cacheUrlReplaceList.addAll((List<HashMap<String, Object>>) obj.get("cacheUrlReplace"));
			}
			if (obj.get("cacheTypeUrl") instanceof Map) {
				cacheTypeUrlMap.putAll((LinkedHashMap<String, String>) obj
						.get("cacheTypeUrl"));
			}
			if (obj.get("fileType") instanceof List) {
				fileTypeSet.addAll((List<String>) obj.get("fileType"));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化配置文件
	 */
	public static void initConfig(AssetManager assets) {
		Yaml yaml = new Yaml();
		try {
			String yamltxt = IOUtil.readStream(assets.open(configName))
					.trim();
			@SuppressWarnings("unchecked")
			LinkedHashMap<String, Object> obj = (LinkedHashMap<String, Object>) yaml
					.load(yamltxt);
			
			System.out.println(obj);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
