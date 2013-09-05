package com.yh.web.cache;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import android.app.Activity;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;

/**
 * 
 * @author gudh 涉及HTTP的处理，异步方法
 */
public class HttpUtil {
	// 异步HTTPClient
	private static AsyncHttpClient client = new AsyncHttpClient();

	/**
	 * 判断输入是否是URL
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isUrl(String url) {
		String regex = "^((https|http)://)?" + "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP形式的URL-
																				// 199.194.52.184
				+ "|" // 允许IP和DOMAIN（域名）
				+ "(www.)?" // www.
				+ "([0-9a-zA-Z][0-9a-zA-Z-]{0,61})?[0-9a-zA-Z]" // 二级域名
				+ "(\\.[a-zA-Z]{2,6})+)" // .com or .com.cn
				+ "(:[0-9]{1,4})?" // :80
				+ "((/?)|" + ".+/?)$";
		if (Pattern.matches(regex, url)) {
			return true;
		}
		return false;
	}

	/**
	 * 获取URL表示的数据类型
	 * 
	 * @param url
	 * @return
	 */
	public static String getUrlType(String url) {
		Set<String> types = MIME.getCacheMimes();
		// 过滤有时间限制的请求
		if(url.matches(".+t=?\\d{10,13}([^\\d].*|$)")){
			return MIME.noneType;
		}
		for(String type : types){
			String uType = type.toUpperCase(Locale.getDefault());
			String regex = ".*\\.(" + type + "|" + uType + ")(\\?[^/]*)?$";
			if (Pattern.matches(regex, url)) {
				return type.toLowerCase(Locale.getDefault());
			}
		}
		// 否则为HTML
		return MIME.defaultType;
	}
	
	/**
	 * 获取URL的MIME
	 * @param url
	 * @return
	 */
	public static String getUrlMime(String url){
		return MIME.getMimeFromType(getUrlType(url));
	}

	public static void main(String[] arg) {
		String[] urls = {
				"http://www.oschina.net/js/2011/fancybox/jquery.fancybox-1.3.4.css",
				"http://www.oschina.net/img/favicon.ico",
				"http://www.oschina.net/js/2011/oschina.js?ver=20121007",
				"http://www.oschina.net/js/poshytip/jquery.poshytip.min.js",
				"http://www.oschina.net/question?catalog=1",
				"http://www.oschina.net/css/oschina2013.css?date=20130724",
				"http://www.oschina.net/translate/good-habits-in-web-development",
				"http://static.oschina.net/uploads/space/2013/0812/162801_Gtqn_179699.jpg",
				"http://static.oschina.net/uploads/user/362/725072_50.jpg?t=1370482795000",
				"http://static.oschina.net/uploads/user/129/259408_50.pdf?t=1372754512000"

		};
		for (String url : urls) {
			System.out.println(getUrlType(url) + "\t" + url);
		}
	}

	/**
	 * 下载URL的内容到文件
	 * 
	 * @param url
	 * @param fileName
	 */
	public static void downUrlToFile(Activity act, String url, String fileName) {
		String[] allowedContentTypes = new String[] {
				"text/html;charset=utf-8", "image/png", "image/jpeg" };
		client.get(url, new MyBinaryHttpResponseHandler(allowedContentTypes,
				act, fileName));
	}

	static class MyBinaryHttpResponseHandler extends BinaryHttpResponseHandler {
		private Activity act;
		private String fileName;

		public MyBinaryHttpResponseHandler(String[] allowedContentTypes,
				Activity act, String fileName) {
			super(allowedContentTypes);
			this.act = act;
			this.fileName = fileName;
		}

		@Override
		public void onSuccess(byte[] fileData) {
			IOUtil.writeExternalFile(fileName, fileData);
			System.out.println();
			Toast.makeText(act, "down ok, size: " + fileData.length,
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onFailure(Throwable e) {
			Toast.makeText(act, "down fail " + e, Toast.LENGTH_LONG).show();
		}

		@Override
		public void onFailure(Throwable e, String response) {
			Toast.makeText(act, "down fail " + e + "\r\n" + response,
					Toast.LENGTH_LONG).show();
		}
	}
}
