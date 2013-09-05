package com.yh.web.cache;

import java.io.InputStream;

import android.app.Activity;
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
		
		CacheObject obj = new CacheObject(url);
		System.out.println(obj.getType() + " " + obj.getMime() + " | " + url);

		WebResourceResponse res = null;
		if (obj.getMime().startsWith("image")) {
			// 图片处理
			res = getImage(url, obj.getMime(), null);
			
		} else if (obj.getMime() == "text/html") {
			// HTML 处理
			
		} else if (obj.getMime() == "application/x-javascript") {
			// JS 处理
			
		} else if (obj.getMime() == "text/css") {
			// CSS 处理
			
		} else if (MIME.fileTypes.contains(obj.getType())) {
			// 文件处理
			
		} else if (obj.getMime() == "none"){
			// 没找到MIME
			
		}
		return res;
	}

	public static WebResourceResponse getHtml(String url, String encoding) {
		// 此处写获取html缓存的方法
		String html = url;

		String mime = "text/html";
		InputStream is = IOUtil.getInputStreamFromString(html, encoding);
		return IOUtil.generateResource(mime, encoding, is);
	}
	
	/**
	 * 获取图片缓存
	 * @param resources
	 * @param url
	 * @param encoding
	 * @return
	 */
	public static WebResourceResponse getImage(String url, String mime,
			String encoding) {
		// 获取缓存路径
		String fileName = CacheObject.getCacheFileName(url, mime);
		InputStream is = IOUtil.readExternalFile(fileName);
		if (is != null){
			System.out.println("Come From Cache: " + url);
		}
		return IOUtil.generateResource(mime, encoding, is);
	}
}
