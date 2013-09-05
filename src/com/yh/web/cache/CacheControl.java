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
			res = getImage(act, url, obj.getFileName(), obj.getMime(), null);
		} else if (obj.getMime().equals("text/html")) {
			// HTML 处理
			res = getHtml(act, url, obj.getFileName(), obj.getMime(), null);
		} else if (obj.getMime().equals("application/x-javascript")) {
			// JS 处理
			//getDefaultInfo(act, url, obj.getFileName(), obj.getMime(), null);
		} else if (obj.getMime().equals("text/css")) {
			// CSS 处理
			//getDefaultInfo(act, url, obj.getFileName(), obj.getMime(), null);
		} else if (MIME.fileTypes.contains(obj.getType())) {
			// 文件处理
			
		} else if (obj.getMime().equals("none")) {
			// 没找到MIME
			
		}
		
		return res;
	}
	
	/**
	 * 获取默认信息
	 * @param url
	 * @param fileName
	 * @param mime
	 * @param encoding
	 * @return
	 */
	public static WebResourceResponse getDefaultInfo(Activity act, String url, String fileName,
			String mime, String encoding) {
		// 获取缓存
		InputStream is = IOUtil.readExternalFile(fileName);
		if (is != null) {
			System.out.println("Come From Cache: " + url);
		}else{
			HttpUtil.downUrlToFile(null, url, fileName);
			return null;
		}
		return IOUtil.generateResource(mime, encoding, is);
	}

	public static WebResourceResponse getHtml(Activity act, String url, String fileName,
			String mime, String encoding) {
		return getDefaultInfo(act, url, fileName, mime, encoding);
	}

	/**
	 * 获取图片缓存
	 * 
	 * @param resources
	 * @param url
	 * @param encoding
	 * @return
	 */
	public static WebResourceResponse getImage(Activity act, String url, String fileName,
			String mime, String encoding) {
		return getDefaultInfo(act, url, fileName, mime, encoding);
	}
}
