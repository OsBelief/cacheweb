package com.yh.web.cache;

import java.io.InputStream;

import com.yh.web.R;

import android.app.Activity;
import android.content.res.Resources;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

/**
 * @author gudh 缓存控制策略
 */
public class CacheControl {

	/**
	 * 捕捉请求，控制缓存策略
	 * 
	 * @param act
	 *            请求Web所在的Activity
	 * @param url
	 *            请求的地址
	 * @return 返回null走远地址，非空则取返回的资源
	 */
	public static WebResourceResponse getResource(Activity act, WebView web,
			String url) {
		WebResourceResponse res = null;
		if (url.endsWith(".png") || url.endsWith(".jpg")) {
			System.out.println("##image: " + url);
			res = getImage(act.getResources(), url, null);
		} else if (url.contains(".html")) {
			System.out.println("##html: " + url);
		} else if (url.contains(".js")) {
			System.out.println("##js: " + url);
		} else if (url.contains(".css")) {
			System.out.println("##css: " + url);
		} else{
			System.out.println("##other: " + url);
		}
		return res;
	}

	public static WebResourceResponse getHtml(String url, String encoding) {
		// 此处写获取html缓存的方法
		String html = url;

		String mime = "text/html";
		InputStream is = CacheUtil.getInputStreamFromString(html, encoding);
		return CacheUtil.generateResource(mime, encoding, is);
	}

	public static WebResourceResponse getImage(Resources resources, String url,
			String encoding) {
		// 此处写获取html缓存的方法
		String mime = "image/png";
		InputStream is = CacheUtil.getInputStreamFromID(resources,
				R.drawable.ic_launcher);
		// is 不为空可以更改为自己想要替换的内容
		is = null;
		return CacheUtil.generateResource(mime, encoding, is);
	}
}
