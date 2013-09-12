package com.yh.web.cache;

import java.sql.Timestamp;

import android.os.Environment;

/**
 * @author gudh
 * 
 *         缓存对象
 */
public class CacheObject {

	public final static String rootPath = Environment
			.getExternalStorageDirectory().getPath() + "/yichaweb/cache/";

	private String id;
	private String url;
	private String host;
	private String type;
	private String mime;

	// 根据创建时间和缓存策略计算是否过期
	private int cachePolicy = -1;
	private Timestamp createTime;
	private boolean isExpire;

	// 使用次数
	private int useCount = 0;

	// 缓存本地文件
	private String fileName;

	public CacheObject() {
	}

	public CacheObject(String url) {
		this.url = url;
		// 延时初始化其他数据
	}

	/**
	 * 根据URL计算文件名
	 * 
	 * @param url
	 * @return
	 */
	public static String getCacheFileName(String url) {
		String host = HttpUtil.getUrlHost(url);
		String mime = HttpUtil.getUrlMime(url);
		String fileName = MD5Util.getFileName(url);
		fileName = new StringBuffer().append(rootPath).append(host).append("/")
				.append(mime).append("/").append(fileName).toString();
		return fileName;
	}

	/**
	 * 根据必要信息获取文件名
	 * @param id
	 * @param host
	 * @param mime
	 * @return
	 */
	public static String getCacheFileName(String id, String host, String mime) {
		String fileName = new StringBuffer().append(id.substring(0, 2))
				.append(id.substring(2, 4)).append(id.substring(4))
				.toString();
		fileName = new StringBuffer().append(rootPath).append(host).append("/")
				.append(mime).append("/").append(fileName).toString();
		return fileName;
	}

	/**
	 * 根据创建时间、当前时间和缓存策略，判断是否过期
	 * 
	 * @param createTime
	 * @param now
	 * @param cachePolicy
	 * @return
	 */
	public static boolean isExpire(Timestamp createTime, long now,
			int cachePolicy) {
		// 此处实现判断过期代码

		return false;
	}

	public String getId() {
		if (id == null) {
			id = MD5Util.digestString(url);
		}
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
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
			isExpire = isExpire(getCreateTime(), now, getCachePolicy());
		}
		return isExpire;
	}

	public int getUseCount() {
		return useCount;
	}

	public void setUseCount(int useCount) {
		this.useCount = useCount;
	}

	public String getFileName() {
		if (fileName == null) {
			fileName = getCacheFileName(getId(), getHost(), getMime());
		}
		return fileName;
	}
}
