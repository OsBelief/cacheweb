package com.yh.web.cache;

import java.sql.Timestamp;

import android.os.Environment;

/**
 * @author gudh
 * 
 *         �������
 */
public class CacheObject {

	public final static String rootPath = Environment
			.getExternalStorageDirectory().getPath() + "/yichaweb/cache/";

	private String id;
	private String url;
	private String host;
	private String type;
	private String mime;

	// ���ݴ���ʱ��ͻ�����Լ����Ƿ����
	private int cachePolicy = -1;
	private Timestamp createTime;
	private boolean isExpire;

	// ʹ�ô���
	private int useCount = 0;

	// ���汾���ļ�
	private String fileName;

	public CacheObject() {
	}

	public CacheObject(String url) {
		this.url = url;
		// ��ʱ��ʼ����������
	}

	/**
	 * ����URL�����ļ���
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
	 * ���ݱ�Ҫ��Ϣ��ȡ�ļ���
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
	 * ���ݴ���ʱ�䡢��ǰʱ��ͻ�����ԣ��ж��Ƿ����
	 * 
	 * @param createTime
	 * @param now
	 * @param cachePolicy
	 * @return
	 */
	public static boolean isExpire(Timestamp createTime, long now,
			int cachePolicy) {
		// �˴�ʵ���жϹ��ڴ���

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
	 * ���ݵ�ǰʱ�䣬�ж��Ƿ���ڣ�������
	 * 
	 * @param now
	 *            Ϊ-1ʱ���жϣ�����ԭ��ֵ
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
