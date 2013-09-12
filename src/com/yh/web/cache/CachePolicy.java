package com.yh.web.cache;

/**
 * @author gudh
 * �������
 */
public class CachePolicy {
	
	public final static CachePolicy htmlCommon = new CachePolicy();
	public final static CachePolicy imageCommon = new CachePolicy();
	public final static CachePolicy cssCommon = new CachePolicy();
	public final static CachePolicy jsCommon = new CachePolicy();
	
	private long cacheTime;

	public CachePolicy(){
		this.cacheTime = 12 * 3600;
	}
	
	public CachePolicy(long cacheTime){
		this.cacheTime = cacheTime;
	}
	
	public long getCacheTime() {
		return cacheTime;
	}

	public void setCacheTime(long cacheTime) {
		this.cacheTime = cacheTime;
	}
	
	/**
	 * �������ͻ�ȡ�������
	 * @param url
	 * @param type
	 * @param mime
	 * @return
	 */
	public static int getCachePolicy(String url, String type, String mime){
		return 0;
	}
}
