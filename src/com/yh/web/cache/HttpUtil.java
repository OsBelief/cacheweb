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
 * @author gudh �漰HTTP�Ĵ����첽����
 */
public class HttpUtil {
	// �첽HTTPClient
	private static AsyncHttpClient client = null;
	private static String[] allowedContentTypes = null;
	
	/**
	 * ���Զ�ʱ��������Ƿ����
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
	 * �ж��Ƿ��п��õ�����
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
	 * �ж��ֻ������Ƿ�
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
	 * �ж������Ƿ���URL
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isUrl(String url) {
		String regex = "^((https|http)://)?"// IP��ʽ��URL-
				+ "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // 199.194.52.184
				+ "|" // ����IP��DOMAIN��������
				+ "(www.)?" // www.
				+ "([0-9a-zA-Z][0-9a-zA-Z-]{0,61})?[0-9a-zA-Z]" // ��������
				+ "(\\.[a-zA-Z]{2,6})+)" // .com or .com.cn
				+ "(:[0-9]{1,4})?" // :80
				+ "((/?)|" + ".+/?)$";
		if (Pattern.matches(regex, url)) {
			return true;
		}
		return false;
	}

	/**
	 * �����Ч��ת��URL����Ч����null
	 * @param url
	 * @return
	 */
	public static String getToUrl(String url){
		if (url.length() > Config.maxUrlLength) {
			return null;
		}
		// �ų�����URL
		List<String> disCacheUrlList = Config.disCacheUrlList;
		for(String reg : disCacheUrlList){
			if (url.matches(reg)) {
				return null;
			}
		}
		// ת��URL
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
	 * ��ȡURL��ʾ����������
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
		// ����ΪHTML
		return MIME.defaultType;
	}

	/**
	 * ��ȡURL��MIME
	 * 
	 * @param url
	 * @return
	 */
	public static String getUrlMime(String url) {
		return MIME.getMimeFromType(getUrlType(url));
	}

	/**
	 * ����URL�����ݵ��ļ�
	 * 
	 * @param url
	 * @param fileName
	 */
	public static void downUrlToFile(Activity act, String url, String fileName) {
		if(netAvailable){
			System.out.println("Save | " + url);
			
			if (!isUrl(url)) {
				Toast.makeText(act, "�������ӵ�ַ����ȷ��" + url, Toast.LENGTH_LONG).show();
				return;
			}
			// ��ӵ�ǰhostΪReferer
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
