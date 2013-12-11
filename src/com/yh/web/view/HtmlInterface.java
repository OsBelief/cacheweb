package com.yh.web.view;

import android.util.Log;
import android.webkit.JavascriptInterface;

public class HtmlInterface {
	
	/**
	 * 返回url和html内容，存储起来
	 * 
	 * @param url
	 * @param html
	 */
	@JavascriptInterface
	public void callbackPageInfo(String url, String html) {
		Log.d("JSInterface", "callbackPageInfo:" + url);
		Log.d("JSInterface", html);
		// 不缓存任何数据
//		CacheObject obj = new CacheObject(url);
//		String fileName = obj.getFileName();
//		boolean res = false;
//		try {
//			IOUtil.writeFileBytes(MainActivity.nowMainAct, fileName,
//					html.getBytes("utf-8"));
//			res = true;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		// 更新数据库
//		CacheControl.orm.updateDB(obj, res);
	}
}
