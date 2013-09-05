package com.yh.web.cache;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import android.app.Activity;
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

	public static void initAsyncHttpClient(String ua) {
		if (client == null) {
			client = new AsyncHttpClient();
		}
		if (allowedContentTypes == null) {
			allowedContentTypes = new String[] { "text/html", "text/html",
					"text/html;charset=utf-8", "text/html;charset=iso-8859-1",
					"image/png", "image/jpeg", "image/gif" };
		}
		client.setUserAgent(ua);
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
	 * ��ȡURL��ʾ����������
	 * 
	 * @param url
	 * @return
	 */
	public static String getUrlType(String url) {
		Set<String> types = MIME.getCacheMimes();
		if (url.length() > 100) {
			return MIME.noneType;
		}
		// ������ʱ�����Ƶ�����
		if (url.matches(".+t=?\\d{10,13}([^\\d].*|$)")) {
			return MIME.noneType;
		}
		for (String type : types) {
			String uType = type.toUpperCase(Locale.getDefault());
			String regex = ".*\\.(" + type + "|" + uType + ")(\\?[^/]*)?$";
			if (Pattern.matches(regex, url)) {
				return type.toLowerCase(Locale.getDefault());
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
				"http://static.oschina.net/uploads/user/129/259408_50.pdf?t=1372754512000" };
		for (String url : urls) {
			System.out.println(getUrlType(url) + "\t" + url);
		}
	}

	/**
	 * ����URL�����ݵ��ļ�
	 * 
	 * @param url
	 * @param fileName
	 */
	public static void downUrlToFile(Activity act, String url, String fileName) {
		System.out.println("start save: " + url + " to " + fileName);

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
				act, fileName));

		System.out.println("end save: " + url + " to " + fileName);
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
			if(fileData != null && fileData.length > 100){
				IOUtil.writeExternalFile(fileName, fileData);
				if (act != null) {
					Toast.makeText(act, "down ok, size: " + fileData.length,
							Toast.LENGTH_LONG).show();
				}
				System.out.println("down ok, size: " + fileData.length);
			}else{
				System.out.println("down fail : receive is null or lt 100");
			}
		}

		@Override
		public void onFailure(Throwable e) {
			if (act != null) {
				Toast.makeText(act, "down fail " + e, Toast.LENGTH_LONG).show();
				System.out.println("down fail " + e);
			}
		}

		@Override
		public void onFailure(Throwable e, String response) {
			if (act != null) {
				Toast.makeText(act, "down fail " + e + "\r\n" + response,
						Toast.LENGTH_LONG).show();
				System.out.println("down fail " + e + "\r\n" + response);
			}
		}
	}
}
