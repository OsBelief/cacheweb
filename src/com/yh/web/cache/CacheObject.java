package com.yh.web.cache;

import android.os.Environment;

/**
 * @author gudh
 * 缓存对象
 */
public class CacheObject {
	
	public final static String rootPath = Environment.getExternalStorageDirectory().getPath() + "/yichaweb/cache/";
	
	private String url;
	private String type;
	private String mime;
	private String fileName;
	private long lastTime;
	private CachePolicy cachePolicy;
	
	public CacheObject(String url){
		this.url = url;
		this.type = HttpUtil.getUrlType(url);
		this.mime = MIME.getMimeFromType(type);
		this.fileName = getCacheFileName(url, mime);
		this.cachePolicy = CachePolicy.getCachePolicy(url, type, mime);
		
		this.lastTime = 0;
	}
	
	/**
	 * 根据URL获取文件名
	 * @param url
	 * @return
	 */
	public static String getCacheFileName(String url){
		String mime = HttpUtil.getUrlMime(url);
		String fileName = MD5Util.getFileName(url);
		fileName = rootPath + mime + "/" + fileName;
		return fileName;
	}
	
	/**
	 * 根据URL和指定的MIME获取文件名
	 * @param url
	 * @param mime
	 * @return
	 */
	public static String getCacheFileName(String url, String mime){
		String fileName = MD5Util.getFileName(url);
		fileName = rootPath + mime + "/" + fileName;
		return fileName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMime() {
		return mime;
	}

	public void setMime(String mime) {
		this.mime = mime;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
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

	public static String getRootpath() {
		return rootPath;
	}
}
