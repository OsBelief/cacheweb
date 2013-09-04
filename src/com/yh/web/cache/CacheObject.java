package com.yh.web.cache;

/**
 * @author gudh
 * 缓存对象
 */
public class CacheObject {
	
	public final static String rootPath = "/mnt/sdcard/yichaweb/cache/";
	
	private String url;
	
	private String mime;
	
	private long lastTime;
	
	private CachePolicy cachePolicy;
	
	/**
	 * 根据URL获取文件名
	 * @param url
	 * @return
	 */
	public static String getFileName(String url){
		String fileName = MD5.getFileName(url);
		fileName = rootPath + fileName;
		return fileName;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMime() {
		return mime;
	}

	public void setMime(String mime) {
		this.mime = mime;
	}

	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	public CachePolicy getCachePolicy() {
		return cachePolicy;
	}

	public void setCachePolicy(CachePolicy cachePolicy) {
		this.cachePolicy = cachePolicy;
	}
}
