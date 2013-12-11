package com.yh.web.cache;

import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.util.Log;
import android.webkit.CookieManager;

import com.yh.web.view.MainActivity;

/**
 * cookie管理
 * 
 * @author gudh
 * @data 2013-12-11
 */
public class CookieManagers {

	private static Activity activity;

	private static final String COOKIE_KEY = "cookie";
	private static String sCookie = null;
	// 记录判断有效cookie
	private static String[] activeCookieKeys = null;
	private static String activeCookie = "";
	private static ConcurrentHashMap<String, Boolean> urlCookieChanged = new ConcurrentHashMap<String, Boolean>(
			20);
	private static final String COOKIE_URL = "http://passport.yicha.cn/user/login.do?op=login";

	public static void initCookieManager(Activity act) {
		activity = act;
		// 设置cookie，初始化设置changed为false
		sCookie = IOUtil.readKeyValue(act, COOKIE_KEY, null);
		// 默认首页不更新，其他均更新
		setCookieChangedOut(MainActivity.DEFAULT_URL);

		// 判断cookie改变的字段
		activeCookieKeys = new String[3];
		activeCookieKeys[0] = "mma";
		activeCookieKeys[1] = "aun";
		activeCookieKeys[2] = "nne";
		// activeCookieKeys[3] = "JSESSIONID";
	}

	/**
	 * 判断某URL的Cookie是否个改变，或在改变Cookie后是否访问
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isCookieChanged(String url) {
		Boolean change = urlCookieChanged.get(url);
		if (change == null) {
			change = true;
		}
		Log.i("Cookie", "Get changed: " + url + " " + change);
		return change;
	}

	/**
	 * 设置当前URL已更新
	 * 
	 * @param url
	 */
	public static void setCookieChangedOut(String url) {
		urlCookieChanged.put(url, false);
		Log.i("Cookie", "Set changed: " + url + " " + false);
	}

	/**
	 * 设置cookie
	 * 
	 * @param nextSet
	 *            true则紧接着下次跳过，false则下一次依然设置
	 */
	public static void setCookie() {
		String cookie = CookieManager.getInstance().getCookie(COOKIE_URL);
		if (cookie == null) {
			return;
		}
		Log.i("SetCookie", "SRC: " + cookie);
		// cookie是否改变
		if (!isCookieChange(cookie)) {
			return;
		}
		// 存入key-value
		IOUtil.writeKeyValue(activity, COOKIE_KEY, cookie);
		HttpUtil.updateCookie(cookie);
		Log.i("SetCookie", "Old: " + sCookie);
		Log.i("SetCookie", "New: " + cookie);
		sCookie = cookie;
		urlCookieChanged.clear();
		Log.i("Cookie", "All cookie changed true");
	}

	/**
	 * 所有的都重新加载一次
	 */
	public static void clearAllCookie() {
		urlCookieChanged.clear();
	}

	/**
	 * 判断cookie是否改变
	 * 
	 * @param cookie
	 * @return
	 */
	public static boolean isCookieChange(String cookie) {
		cookie = cookie.trim();
		if (cookie.equals("null") || cookie.equals(sCookie)) {
			return false;
		}

		String[] fields = cookie.split(";");
		String csb = "";
		for (String key : activeCookieKeys) {
			for (String field : fields) {
				int in = field.indexOf('=');
				if (in == -1) {
					continue;
				}
				String k = field.substring(0, in).trim();
				if (key.equals(k)) {
					csb += field;
					break;
				}
			}
		}

		boolean change = true;
		if (activeCookie.equals(csb)) {
			change = false;
		} else {
			change = true;
			activeCookie = csb;
		}
		return change;
	}

	public static String getCookie() {
		return sCookie;
	}

}
