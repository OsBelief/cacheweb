package com.yh.web.cache;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;

/**
 * 
 * @author gudh 涉及HTTP的处理，异步方法
 */
public class HttpUtil {
	// 异步HTTPClient
	private static AsyncHttpClient client = null;
	private static String[] allowedContentTypes = null;
	
	/**
	 * 可以定时检查网络是否可用
	 */
	private static boolean netAvailable = true;

	public static void initAsyncHttpClient(String ua) {
		if (client == null) {
			client = new AsyncHttpClient();
		}
		if (allowedContentTypes == null) {
			allowedContentTypes = new String[] {".*"};
		}
		client.setUserAgent(ua);
	}
	
	/**
	 * 判断是否有可用的网络
	 * @param context
	 * @return
	 */
	public static boolean isNetAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo workInfo = cm.getActiveNetworkInfo();
		if(workInfo != null){
			return workInfo.isAvailable();
		}
		return false;
	}
	
	/**
	 * 判断手机网络是否
	 * @param context
	 * @return
	 */
	public static boolean isMobileNetAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo workInfo = cm.getActiveNetworkInfo();
		if (workInfo != null && workInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
			return true;
		}
		return false;
	}

	/**
	 * 判断输入是否是URL
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isUrl(String url) {
		String regex = "^((https|http)://)?"// IP形式的URL-
				+ "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // 199.194.52.184
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
	 * 如果有效，转换URL，无效返回null
	 * @param url
	 * @return
	 */
	public static String getToUrl(String url){
		if (url.length() > Config.maxUrlLength) {
			return null;
		}
		// 排除过滤URL
		List<String> disCacheUrlList = Config.disCacheUrlList;
		for(String reg : disCacheUrlList){
			if (url.matches(reg)) {
				return null;
			}
		}
		// 转换URL
		List<HashMap<String, Object>> cacheUrlReplaceList = Config.cacheUrlReplaceList;
		for(HashMap<String, Object> one : cacheUrlReplaceList){
			Matcher m = Pattern.compile(one.get("src").toString()).matcher(url);
			if (m.find()) {
				url = m.replaceFirst(one.get("dest").toString());
				System.out.println("Match | dest : " + url);
				break;
			}
		}
		return url;
	}
	
	/**
	 * 获取URL表示的数据类型
	 * 
	 * @param url
	 * @return
	 */
	public static String getUrlType(String url) {
		Map<String, String> typeReg = Config.cacheTypeUrlMap;
		Set<String> types = typeReg.keySet();
		for (String type : types) {
			String regex = typeReg.get(type);
			if (Pattern.matches(regex, url)) {
				return type;
			}
		}
		// 否则为HTML
		return MIME.defaultType;
	}

	/**
	 * 获取URL的MIME
	 * 
	 * @param url
	 * @return
	 */
	public static String getUrlMime(String url) {
		return MIME.getMimeFromType(getUrlType(url));
	}

	/**
	 * 下载URL的内容到文件
	 * 
	 * @param url
	 * @param fileName
	 */
	public static void downUrlToFile(Activity act, String url, String fileName) {
		if(netAvailable){
			System.out.println("Save | " + url);
			
			if (!isUrl(url)) {
				Toast.makeText(act, "下载链接地址不正确！" + url, Toast.LENGTH_LONG).show();
				return;
			}
			// 添加当前host为Referer
			try {
				URL u = new URL(url);
				client.addHeader("Referer", u.getProtocol() + "://" + u.getHost());
			} catch (MalformedURLException e) {
			}
	
			client.get(url, new MyBinaryHttpResponseHandler(allowedContentTypes,
					act, url, fileName));
		} else{
			System.out.println("Net is not available " + url);
		}
	}

	static class MyBinaryHttpResponseHandler extends BinaryHttpResponseHandler {
		private Activity act;
		private String fileName;
		private String url;
		private int maxAllowByteLen = 50;

		public MyBinaryHttpResponseHandler(String[] allowedContentTypes,
				Activity act, String url, String fileName) {
			super(allowedContentTypes);
			this.act = act;
			this.url = url;
			this.fileName = fileName;
		}

		@Override
		public void onSuccess(byte[] fileData) {
			if (fileData != null && fileData.length > maxAllowByteLen) {
				IOUtil.writeExternalFile(fileName, fileData);
				if (act != null) {
					Toast.makeText(act, "Down ok, size: " + fileData.length,
							Toast.LENGTH_LONG).show();
				}
				System.out.println("Down ok, size: " + fileData.length + " " + url);
			} else {
				System.out.println("Down fail : receive is null or len is "
						+ fileData.length + " lt " + maxAllowByteLen + " " + url);
			}
		}

		@Override
		public void onFailure(Throwable e) {
			if (act != null) {
				Toast.makeText(act, "Down fail " + e, Toast.LENGTH_LONG).show();
			}
			System.out.println("Down fail " + e + " " + url);
		}

		@Override
		public void onFailure(Throwable e, String response) {
			if (act != null) {
				Toast.makeText(act, "Down fail " + e + "\r\n" + response,
						Toast.LENGTH_LONG).show();
			}
			System.out.println("Down fail " + e + "\r\n" + response + " " + url);
		}
	}
}
