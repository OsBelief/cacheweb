package com.yh.web.cache;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import cn.yicha.cache.fuli.R;

import com.yh.web.cache.db.CacheOrm;

/**
 * @author gudh 缓存控制策略
 */
public class CacheControl {

	public static CacheOrm orm = null;
	
	public static String defaultUrl;
	
	public static boolean isFirst = true;
	
	/**
	 * 初始化缓存ORM
	 * 
	 * @param context
	 */
	public static void initCache(Context context) {
		orm = new CacheOrm(context);
		defaultUrl = ((Activity)context).getString(R.string.defaultUrl);
	}

	/**
	 * 捕捉请求，控制缓存策略
	 * 
	 * @param context
	 *            请求Web所在的Activity
	 * @param url
	 *            请求的地址
	 * @return 返回null走远地址，非空则取返回的资源
	 */
	public static WebResourceResponse getResource(Context context, WebView web,
			String url) {
		WebResourceResponse res = null;
		
		// 任何登录链接重置Cookie
		if(url.startsWith("http://passport.yicha.cn/user/login")){
			CookieManagers.clearAllCookie();
		}
		// 获取转换的URL
		String urlb = url;
		url = HttpUtil.getToUrl(url);
		if (url == null) {
			if(!urlb.contains("passport")){
				Log.i("getResource", "DisCache url | " + urlb);
			}
			// 如果转换的URL为null，则表示不需要缓存
			return null;
		}
		
		// 如果是主页单独判断
		if (defaultUrl.equals(url)) {
			return getMainPageResponse(context, url);
		}
		
		boolean fromCache = true;
		// 查询数据库是否有缓存
		CacheObject obj = orm.queryByUrl(url);
		if (obj == null) {
			obj = new CacheObject(url);
			fromCache = false;
		}
		if (!CacheFilter.passHostFilter(obj)) {
			Log.i("getResource", "DisCache host type url | " + urlb);
			// 如果转换的URL为null，则表示不需要缓存
			return null;
		}

		if (CacheFilter.notCacheType.contains(obj.getType())) {
			// 不缓存处理
			Log.i("getResource", "DisCache type url | " + urlb);
			// 如果转换的URL为null，则表示不需要缓存
			return null;
		}
		
		if (obj.getMime().startsWith("image")) {
			// 图片处理
			res = getImage(context, obj, null);
		} else if (obj.getMime().equals("text/html")) {
			// HTML 处理
			res = getHtml(context, obj, null);
		} else if (obj.getMime().equals("application/x-javascript")) {
			// JS 处理
			res = getDefaultInfo(context, obj, null);
		} else if (obj.getMime().equals("text/css")) {
			// CSS 处理
			res = getDefaultInfo(context, obj, null);
		} else if (obj.getMime().equals("none")) {
			// 没找到MIME

		}

		if (fromCache) {
			Log.i("getResource",
					"From Cache | " + obj.getType() + " " + obj.getMime()
							+ " | " + url);
		} else {
			Log.i("getResource",
					"From Net | " + obj.getType() + " " + obj.getMime()
							+ " | " + url);
		}
		return res;
	}

	/**
	 * 获取主页的数据，从缓存取，从main.htm取
	 * @param context
	 * @param url
	 * @return
	 */
	public static WebResourceResponse getMainPageResponse(Context context, String url){
		CacheObject obj = new CacheObject(url);
		// cookie改变直接返回
		if (CookieManagers.isCookieChanged(url)) {
			orm.delete(obj);
			Log.i("getResource", "Main cookie change | " + url);
			return null;
		}
		// 从缓存中取
		WebResourceResponse res = getDefaultInfo(context, obj, null, false);
		if (res == null) {
			try {
				// 否则从main.htm中获取
				InputStream is = context.getAssets().open("main.htm");
				Log.i("getResource",
						"Use main.htm, Cookie is not change and Cache is not exist | " + url);
				res = IOUtil.generateResource(MIME.getMimeFromType("htm"),
						null, is);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Log.i("getResource", "From Cache | " + obj.getType() + " "
					+ obj.getMime() + " | " + url);
		}
		return res;
	}
	
	/**
	 * 获取默认信息，如果不存在或过期则下载
	 * 
	 * @param context
	 * @param obj
	 * @param encoding
	 * @return
	 */
	public static WebResourceResponse getDefaultInfo(Context context,
			CacheObject obj, String encoding) {
		return getDefaultInfo(context, obj, encoding, true);
	}
	
	/**
	 * 获取默认信息
	 * 
	 * @param context
	 * @param obj
	 * @param encoding
	 * @param needDown 如果为false则不会下载，否则根据情况下载
	 * @return
	 */
	public static WebResourceResponse getDefaultInfo(Context context,
			CacheObject obj, String encoding, boolean needDown) {
		// 是否需要更新缓存
		boolean needUpdate = false;
		// 返回结果
		WebResourceResponse res = null;

		if (obj.isComeFromCache()) {
			// 来自缓存
			if (!obj.isExpire(System.currentTimeMillis())) {
				// 缓存未过期
				InputStream is = IOUtil.getFileInputStream((Activity)context, obj.getFileName());
				if (is != null) {
					// 缓存文件仍然存在
					res = IOUtil.generateResource(obj.getMime(), encoding, is);
					obj.setUseCount(obj.getUseCount() + 1);
					Log.d("updateDB", "UseCount " + obj.getUseCount() + " | "
							+ obj.getUrl());
					orm.updateUseCount(obj);
				} else {
					Log.i("getDefaultInfo", "File NotExist | " + obj.getUrl()
							+ " " + obj.getFileName());
					needUpdate = true;
				}
			} else {
				Log.i("getDefaultInfo", "Cache Expire | " + obj.getUrl());
				needUpdate = true;
			}
		} else {
			Log.i("getDefaultInfo", "Need Down | " + obj.getUrl());
			needUpdate = true;
		}
		if (needDown && needUpdate) {
			// 更新缓存
			HttpUtil.downUrlToFile(null, obj);
		}
		return res;
	}

	/**
	 * 获取html缓存
	 * 
	 * @param context
	 * @param obj
	 * @param encoding
	 * @return
	 */
	public static WebResourceResponse getHtml(Context context, CacheObject obj,
			String encoding) {
		return getDefaultInfo(context, obj, encoding);
	}

	/**
	 * 获取图片缓存
	 * 
	 * @param context
	 * @param obj
	 * @param encoding
	 * @return
	 */
	public static WebResourceResponse getImage(Context context,
			CacheObject obj, String encoding) {
		return getDefaultInfo(context, obj, encoding);
	}
}
