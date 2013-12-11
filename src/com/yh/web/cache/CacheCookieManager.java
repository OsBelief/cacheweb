package com.yh.web.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * cookie管理，简单保存cookie改变状态即可
 * 
 * @author gudh
 * @data 2013-12-11
 */
public class CacheCookieManager {

	private static ConcurrentHashMap<String, Boolean> cookieChangedMap = new ConcurrentHashMap<String, Boolean>();
	
	/**
	 * 
	 * @param url
	 * @param changed true改变，false未改变
	 */
	public static void setCookieChanged(String url, Boolean changed) {
		cookieChangedMap.put(url, changed);
	}

	/**
	 * 返回true则改变，false未改变。默认未改变
	 * @param url
	 * @return
	 */
	public static boolean isCookieChanged(String url) {
		boolean changed = false;
		if (cookieChangedMap.containsKey(url)) {
			changed = cookieChangedMap.get(url);
		}
		return changed;
	}
}
