package com.yh.web.cache;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
import android.util.Log;
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
			allowedContentTypes = new String[] { ".*" };
		}
		client.setUserAgent(ua);
	}

	/**
	 * �ж��Ƿ��п��õ�����
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo workInfo = cm.getActiveNetworkInfo();
		if (workInfo != null) {
			return workInfo.isAvailable();
		}
		return false;
	}

	/**
	 * �ж��ֻ������Ƿ�
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isMobileNetAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo workInfo = cm.getActiveNetworkInfo();
		if (workInfo != null
				&& workInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
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
	 * 
	 * @param url
	 * @return
	 */
	public static String getToUrl(String url) {
		if (url.length() > Config.maxUrlLength) {
			return null;
		}
		// �ų�����URL
		List<String> disCacheUrlList = Config.disCacheUrlList;
		for (String reg : disCacheUrlList) {
			if (url.matches(reg)) {
				return null;
			}
		}
		// ת��URL
		String urlb = url;
		List<HashMap<String, Object>> cacheUrlReplaceList = Config.cacheUrlReplaceList;
		for (HashMap<String, Object> one : cacheUrlReplaceList) {
			Matcher m = Pattern.compile(one.get("src").toString()).matcher(url);
			if (m.find()) {
				url = m.replaceFirst(one.get("dest").toString());
				Log.i("getToUrl", "Match | " + urlb + " to " + url);
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
	 * ��ȡURL��host
	 * 
	 * @param url
	 * @return
	 */
	public static String getUrlHost(String url) {
		try {
			return new URI(url).getHost();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ����URL�����ݵ��ļ�
	 * 
	 * @param url
	 * @param fileName
	 */
	public static void downUrlToFile(Activity act, String url, String fileName) {
		if (netAvailable) {
			Log.i("downUrlToFile", "Save | " + url);

			if (!isUrl(url)) {
				Toast.makeText(act, "�������ӵ�ַ����ȷ��" + url, Toast.LENGTH_LONG)
						.show();
				return;
			}
			// ��ӵ�ǰhostΪReferer
			try {
				URL u = new URL(url);
				client.addHeader("Referer",
						u.getProtocol() + "://" + u.getHost());
			} catch (MalformedURLException e) {
			}

			client.get(url, new MyBinaryHttpResponseHandler(
					allowedContentTypes, act, url, fileName));
		} else {
			Log.i("downUrlToFile", "Net is not available " + url);
		}
	}

	/**
	 * ���ػ���
	 * 
	 * @param act
	 * @param obj
	 */
	public static void downUrlToFile(Activity act, CacheObject obj) {
		if (netAvailable) {
			Log.i("downUrlToFile", "Save | " + obj.getUrl());

			try {
				URL u = new URL(obj.getUrl());
				// ��ӵ�ǰhostΪReferer
				client.addHeader("Referer",
						u.getProtocol() + "://" + u.getHost());
			} catch (MalformedURLException e) {
			}

			client.get(obj.getUrl(), new MyBinaryHttpResponseHandler(
					allowedContentTypes, act, obj));
		} else {
			Log.i("downUrlToFile", "Net is not available " + obj.getUrl());
		}
	}

	static class MyBinaryHttpResponseHandler extends BinaryHttpResponseHandler {
		private Activity act;
		private String fileName;
		private String url;
		private int maxAllowByteLen = 50;
		private CacheObject cacheObj;

		public MyBinaryHttpResponseHandler(String[] allowedContentTypes,
				Activity act, String url, String fileName) {
			super(allowedContentTypes);
			this.act = act;
			this.url = url;
			this.fileName = fileName;
		}

		public MyBinaryHttpResponseHandler(String[] allowedContentTypes,
				Activity act, CacheObject obj) {
			super(allowedContentTypes);
			this.act = act;
			this.url = obj.getUrl();
			this.fileName = obj.getFileName();
			this.cacheObj = obj;
		}

		/**
		 * �������ݿ�
		 * 
		 * @param obj
		 * @param result
		 */
		public void updateDB(CacheObject obj, boolean result) {
			try {
				if (!result) {
					// ʧ����ɾ�����ݿ��¼
					if (obj != null && obj.isComeFromCache()) {
						CacheControl.orm.delete(obj);
						Log.i("updateDB", "DELETE | " + obj.getUrl());
					}
				} else {
					// �ɹ�����������´���ʱ��
					obj.setCreateTime(System.currentTimeMillis());
					if (obj != null && obj.isComeFromCache()) {
						CacheControl.orm.updateTime(obj);
						Log.i("updateDB", "UPTIME | " + obj.getUrl());
					} else {
						CacheControl.orm.add(obj);
						Log.i("updateDB", "INSERT | " + obj.getUrl());
					}
				}
			} catch (Exception e) {
				Log.e("updateDB", obj.getUrl() + "\t" + e.getMessage());
			}
		}

		@Override
		public void onSuccess(byte[] fileData) {
			boolean res = false;
			if (fileData != null && fileData.length > maxAllowByteLen) {
				IOUtil.writeExternalFile(fileName, fileData);
				if (act != null) {
					Toast.makeText(act, "Down Ok, size: " + fileData.length,
							Toast.LENGTH_LONG).show();
				}
				res = true;
				Log.i("downUrlToFile", "Down Ok, size: " + fileData.length
						+ " " + url);
			} else {
				Log.i("downUrlToFile", "Down Fail : receive is null or len is "
						+ fileData.length + " lt " + maxAllowByteLen + " "
						+ url);
			}
			updateDB(cacheObj, res);
		}

		@Override
		public void onFailure(Throwable e) {
			if (act != null) {
				Toast.makeText(act, "Down Fail " + e, Toast.LENGTH_LONG).show();
			}
			Log.i("downUrlToFile", "Down Fail " + e + " " + url);
			updateDB(cacheObj, false);
		}

		@Override
		public void onFailure(Throwable e, String response) {
			if (act != null) {
				Toast.makeText(act, "Down Fail " + e + "\r\n" + response,
						Toast.LENGTH_LONG).show();
			}
			Log.i("downUrlToFile", "Down Fail " + e + "\r\n" + response + " "
					+ url);
			updateDB(cacheObj, false);
		}
	}
}
