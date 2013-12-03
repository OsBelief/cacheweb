package com.yh.web.cache;

import android.os.Environment;

/**
 * @author gudh
 * 
 *         缓存对象
 */
public class CacheObject {

	public final static String rootPath = Environment
			.getExternalStorageDirectory().getPath() + "/yichaweb/cache/";

	public final static boolean useExtern = true;
	public final static boolean multiPath = false;
	
	private String uid;
	private String url;
	private String host;
	private String type;
	private String mime;

	// 缓存本地文件
	private String fileName;

	// 根据创建时间和缓存策略计算是否过期
	private int cachePolicy = -1;
	private long createTime;

	// 使用次数
	private int useCount = 0;

	// isExpire 不存数据库，用时判断
	private boolean isExpire;
	// 来自缓存，从数据库获取是设置为true， 否则不设置
	private boolean comeFromCache = false;

	public CacheObject() {
	}

	public CacheObject(String url) {
		this.url = url;
		// 延时初始化其他数据
	}

	/**
	 * 根据URL计算文件名，存储用户下载的内容
	 * 
	 * @param url
	 * @return
	 */
	public static String getCacheFileName(String url) {
		String mime = HttpUtil.getUrlMime(url);
		String fileName = MD5Util.getFileName(url);
		fileName = new StringBuffer().append(rootPath).append("UserDown")
				.append("/").append(mime).append("/").append(fileName)
				.toString();
		return fileName;
	}

	/**
	 * 根据必要信息获取文件名，存储程序下载的内容
	 * 
	 * @param id
	 * @param host
	 * @param mime
	 * @return
	 */
	public static String getCacheFileName(String id, String host, String mime) {
		// 判断使用内部还是外部存储缓存
		if(useExtern){
			return getCacheExternFileName(id, host, mime);
		} else{
			return getCacheInnerFileName(id, host, mime);
		}
	}

	/**
	 * 获取外部文件名
	 * 
	 * @param id
	 * @param host
	 * @param mime
	 * @return
	 */
	public static String getCacheExternFileName(String id, String host,
			String mime) {
		String fileName = id;
		if (multiPath) {
			fileName = new StringBuffer().append(rootPath).append(host)
					.append("/").append(mime).append("/")
					.append(id.substring(0, 2)).append("/")
					.append(id.substring(10)).toString();
		} else {
			fileName = new StringBuffer().append(rootPath).append("cfile/")
					.append(id.substring(0, 1)).append("/")
					.append(id.substring(10)).toString();
		}
		return fileName;
	}
	
	/**
	 * 获取内部文件名
	 * @param id
	 * @param host
	 * @param mime
	 * @return
	 */
	public static String getCacheInnerFileName(String id, String host, String mime) {
		return id;
	}

	public String getUid() {
		if (uid == null) {
			uid = MD5Util.digestString(url);
		}
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHost() {
		if (host == null) {
			host = HttpUtil.getUrlHost(url);
		}
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getType() {
		if (type == null) {
			type = HttpUtil.getUrlType(url);
		}
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMime() {
		if (mime == null) {
			mime = MIME.getMimeFromType(getType());
		}
		return mime;
	}

	public void setMime(String mime) {
		this.mime = mime;
	}

	public String getFileName() {
		if (fileName == null) {
			fileName = getCacheFileName(getUid(), getHost(), getMime());
		}
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getCachePolicy() {
		if (cachePolicy == -1) {
			cachePolicy = CachePolicy.getCachePolicy(getUrl(), getType(),
					getMime());
		}
		return cachePolicy;
	}

	public void setCachePolicy(int cachePolicy) {
		this.cachePolicy = cachePolicy;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public int getUseCount() {
		return useCount;
	}

	public void setUseCount(int useCount) {
		this.useCount = useCount;
	}

	/**
	 * 根据当前时间，判断是否过期，并设置
	 * 
	 * @param now
	 *            为-1时不判断，返回原有值
	 * @return
	 */
	public boolean isExpire(long now) {
		if (now != -1) {
			isExpire = CachePolicy.isExpire(getCreateTime(), now,
					getCachePolicy());
		}
		return isExpire;
	}

	public boolean isComeFromCache() {
		return comeFromCache;
	}

	public void setComeFromCache(boolean comeFromCache) {
		this.comeFromCache = comeFromCache;
	}
}
