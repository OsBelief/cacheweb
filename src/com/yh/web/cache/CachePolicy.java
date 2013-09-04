package com.yh.web.cache;

/**
 * @author gudh
 * »º´æ²ßÂÔ
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
}
