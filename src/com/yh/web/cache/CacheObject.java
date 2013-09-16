package com.yh.web.cache;

import android.os.Environment;

/**
 * @author gudh
 * 
 *         �������
 */
public class CacheObject {

	public final static String rootPath = Environment
			.getExternalStorageDirectory().getPath() + "/yichaweb/cache/";

	private String uid;
	private String url;
	private String host;
	private String type;
	private String mime;

	// ���汾���ļ�
	private String fileName;

	// ���ݴ���ʱ��ͻ�����Լ����Ƿ����
	private int cachePolicy = -1;
	private long createTime;

	// ʹ�ô���
	private int useCount = 0;
	
	// isExpire �������ݿ⣬��ʱ�ж�
	private boolean isExpire;
	// ���Ի��棬�����ݿ��ȡ������Ϊtrue�� ��������
	private boolean comeFromCache = false;
	
	public CacheObject() {
	}

	public CacheObject(String url) {
		this.url = url;
		// ��ʱ��ʼ����������
	}

	/**
	 * ����URL�����ļ������洢�û����ص�����
	 * 
	 * @param url
	 * @return
	 */
	public static String getCacheFileName(String url) {
		String mime = HttpUtil.getUrlMime(url);
		String fileName = MD5Util.getFileName(url);
		fileName = new StringBuffer().append(rootPath).append("UserDown").append("/")
				.append(mime).append("/").append(fileName).toString();
		return fileName;
	}

	/**
	 * ���ݱ�Ҫ��Ϣ��ȡ�ļ������洢�������ص�����
	 * @param id
	 * @param host
	 * @param mime
	 * @return
	 */
	public static String getCacheFileName(String id, String host, String mime) {
		String fileName = new StringBuffer().append(id.substring(0, 2))
				.append("/").append(id.substring(10))
				.toString();
		fileName = new StringBuffer().append(rootPath).append(host).append("/")
				.append(mime).append("/").append(fileName).toString();
		return fileName;
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
	 * ���ݵ�ǰʱ�䣬�ж��Ƿ���ڣ�������
	 * 
	 * @param now
	 *            Ϊ-1ʱ���жϣ�����ԭ��ֵ
	 * @return
	 */
	public boolean isExpire(long now) {
		if (now != -1) {
			isExpire = CachePolicy.isExpire(getCreateTime(), now, getCachePolicy());
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
