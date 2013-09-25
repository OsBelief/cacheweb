package com.yh.web.cache;

import java.io.InputStream;

import android.content.Context;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.yh.web.cache.db.CacheOrm;

/**
 * @author gudh 缓存控制策略
 */
public class CacheControl {

	public static CacheOrm orm = null;

	/**
	 * 初始化缓存ORM
	 * 
	 * @param context
	 */
	public static void initCache(Context context) {
		orm = new CacheOrm(context);
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
		// 获取转换的URL
		String urlb = url;
		url = HttpUtil.getToUrl(url);
		if (url == null) {
			Log.i("getResource", "DisCache url | " + urlb);
			// 如果转换的URL为null，则表示不需要缓存
			return null;
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

		WebResourceResponse res = null;
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
		} else if (CacheFilter.notCacheType.contains(obj.getType())) {
			// 不缓存处理

		} else if (obj.getMime().equals("none")) {
			// 没找到MIME

		}

		if (fromCache) {
			Log.i("getResource",
					"From Cache | " + obj.getType() + " " + obj.getMime()
							+ " | " + url);
		} else {
			Log.i("getResource",
					"New Fetch | " + obj.getType() + " " + obj.getMime()
							+ " | " + url);
		}
		return res;
	}

	/**
	 * 获取默认信息
	 * 
	 * @param context
	 * @param obj
	 * @param encoding
	 * @return
	 */
	public static WebResourceResponse getDefaultInfo(Context context,
			CacheObject obj, String encoding) {
		// 是否需要更新缓存
		boolean needUpdate = false;
		// 返回结果
		WebResourceResponse res = null;

		if (obj.isComeFromCache()) {
			// 来自缓存
			if (!obj.isExpire(System.currentTimeMillis())) {
				// 缓存未过期
				InputStream is = IOUtil.readExternalFile(obj.getFileName());
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
			Log.i("getDefaultInfo", "NeedUpdate | " + obj.getUrl());
			needUpdate = true;
		}
		if (needUpdate) {
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
