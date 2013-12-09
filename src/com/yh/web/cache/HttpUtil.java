package com.yh.web.cache;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.params.ClientPNames;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.yh.web.view.MainActivity;

/**
 * 
 * @author gudh 涉及HTTP的处理，异步方法
 */
public class HttpUtil {
	// 异步HTTPClient
	private static AsyncHttpClient client = null;
	private static String[] allowedContentTypes = null;
	private static Activity activity = null;
	
	private static final String COOKIE_KEY = "cookie";
	private static String sCookie = null;
	// 记录判断有效cookie
	private static String[] activeCookieKeys = null;
	private static String activeCookie = "";
	private static ConcurrentHashMap<String, Boolean> urlCookieChanged = new ConcurrentHashMap<String, Boolean>(20);
	private static final String COOKIE_URL = "http://passport.yicha.cn/user/login.do?op=login";
	
	/**
	 * 可以定时检查网络是否可用
	 */
	private static boolean netAvailable = true;

	public static void initAsyncHttpClient(Activity act, ThreadPoolExecutor threadPool, String ua) {
		activity = act;
		if (client == null) {
			client = new AsyncHttpClient();
			client.setThreadPool(threadPool);
		}
		// 设置302不跳转
		client.getHttpClient().getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		
		if (allowedContentTypes == null) {
			allowedContentTypes = new String[] { ".*" };
		}
		client.setUserAgent(ua);
		// 设置cookie，初始化设置changed为false
		sCookie = IOUtil.readKeyValue(act, COOKIE_KEY, null);
		// 默认首页不更新，其他均更新
		setCookieChangedOut(MainActivity.DEFAULT_URL);
		
		// 判断cookie改变的字段
		activeCookieKeys = new String[4];
		activeCookieKeys[0] = "mma";
		activeCookieKeys[1] = "aun";
		activeCookieKeys[2] = "nne";
		activeCookieKeys[3] = "JSESSIONID";
	}

	/**
	 * 判断某URL的Cookie是否个改变，或在改变Cookie后是否访问
	 * @param url
	 * @return
	 */
	public static boolean isCookieChanged(String url) {
		Boolean change = urlCookieChanged.get(url);
		if(change == null){
			change = true;
		}
		Log.i("Cookie", "Get changed: " + url + " " + change);
		return change;
	}

	/**
	 * 设置当前URL已更新
	 * @param url
	 */
	public static void setCookieChangedOut(String url) {
		urlCookieChanged.put(url, false);
		Log.i("Cookie", "Set changed: " +url + " " + false);
	}

	/**
	 * 设置cookie
	 * @param nextSet true则紧接着下次跳过，false则下一次依然设置
	 */
	public synchronized static void setCookie() {
		String cookie = CookieManager.getInstance().getCookie(COOKIE_URL);
		if(cookie == null){
			return;
		}
		Log.i("SetCookie", "SRC: " + cookie);
		// cookie是否改变
		if(!isCookieChange(cookie)){
			return;
		}
		// 存入key-value
		IOUtil.writeKeyValue(activity, COOKIE_KEY, cookie);
		client.addHeader("Cookie", cookie);
		Log.i("SetCookie", "Old: " + sCookie);
		Log.i("SetCookie", "New: " + cookie);
		sCookie = cookie;
		urlCookieChanged.clear();
		Log.i("Cookie", "All cookie changed true");
	}
	
	/**
	 * 所有的都重新加载一次
	 */
	public static void clearAllCookie(){
		urlCookieChanged.clear();
	}
	
	/**
	 * 判断cookie是否改变
	 * @param cookie
	 * @return
	 */
	public synchronized static boolean isCookieChange(String cookie){
		cookie = cookie.trim();
		if (cookie.equals("null") || cookie.equals(sCookie)) {
			return false;
		}
		
		String[] fields = cookie.split(";");
		String csb = "";
		for(String key : activeCookieKeys){
			for(String field : fields){
				int in = field.indexOf('=');
				if(in == -1){
					continue;
				}
				String k = field.substring(0, in).trim();
				if(key.equals(k)){
					csb += field;
					break;
				}
			}
		}
		
		boolean change = true;
		if(activeCookie.equals(csb)){
			change = false;
		} else{
			change = true;
			activeCookie = csb;
		}
		return change;
	}
	
	public static String getCookie(){
		return HttpUtil.sCookie;
	}
	
	/**
	 * 判断是否有可用的网络
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
	 * 判断手机网络是否
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
	 * 
	 * @param url
	 * @return
	 */
	public static String getToUrl(String url) {
		if (url.length() > CacheFilter.maxUrlLength) {
			return null;
		}
		// 排除过滤URL
		List<String> disCacheUrlList = CacheFilter.disCacheUrlList;
		for (String reg : disCacheUrlList) {
			if (url.matches(reg)) {
				return null;
			}
		}
		// 转换URL
		String urlb = url;
		List<HashMap<String, Object>> cacheUrlReplaceList = CacheFilter.cacheUrlReplaceList;
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
	 * 获取URL表示的数据类型
	 * 
	 * @param url
	 * @return
	 */
	public static String getUrlType(String url) {
		Map<String, String> typeReg = CacheFilter.cacheTypeUrlMap;
		Set<String> types = typeReg.keySet();
		for (String type : types) {
			String regex = typeReg.get(type);
			if (Pattern.matches(regex, url)) {
				return type;
			}
		}
		// 否则为HTML
		return MIME.DEFAULT_TYPE;
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
	 * 获取URL的host
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
	 * 下载URL的内容到文件
	 * 
	 * @param url
	 * @param fileName
	 */
	public static void downUrlToFile(Activity act, String url, String fileName) {
		if (netAvailable) {
			Log.i("downUrlToFile", "Save | " + url);

			if (!isUrl(url)) {
				Toast.makeText(act, "下载链接地址不正确！" + url, Toast.LENGTH_LONG)
						.show();
				return;
			}
			// 添加当前host为Referer
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
	 * 下载缓存
	 * 
	 * @param act
	 * @param obj
	 */
	public static void downUrlToFile(Activity act, CacheObject obj) {
		if (netAvailable) {
			Log.i("downUrlToFile", "Save | " + obj.getUrl());

			try {
				URL u = new URL(obj.getUrl());
				// 添加当前host为Referer
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
		 * 更新数据库
		 * 
		 * @param obj
		 * @param result
		 */
		public void updateDB(CacheObject obj, boolean result) {
			try {
				if (!result) {
					// 失败则删除数据库记录
					if (obj != null && obj.isComeFromCache()) {
						CacheControl.orm.delete(obj);
						Log.i("updateDB", "DELETE | " + obj.getUrl());
					}
				} else {
					// 成功并存在则更新创建时间
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
				e.printStackTrace();
				Log.e("updateDB", obj.getUrl() + "\t" + e.getMessage());
			}
		}

		@Override
		public void onSuccess(byte[] fileData) {
			boolean res = false;
			if (fileData != null && fileData.length > maxAllowByteLen) {
				// 以/开头写外部文件，否则写内部文件
				if(fileName.startsWith("/")){
					IOUtil.writeExternalFile(fileName, fileData);
				} else {
					IOUtil.writeInternalFile(activity, fileName, fileData);
				}
				if (act != null) {
					Toast.makeText(act, "Down Ok, size: " + fileData.length,
							Toast.LENGTH_LONG).show();
				}
				res = true;
				Log.i("downUrlToFile", "Down Ok, size: " + fileData.length
						+ " | " + url);
			} else {
				Log.i("downUrlToFile", "Down Fail : receive is null or len is "
						+ fileData.length + " lt " + maxAllowByteLen + " | "
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
