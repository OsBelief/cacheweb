package com.yh.web.cache;

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
	private static AsyncHttpClient client = new AsyncHttpClient();

	public static boolean isUrl(String url){
		String regex = "(([\\w]+:)?//)?(([\\d\\w]|%[a-fA-f\\d]{2,2})+(:([\\d\\w]|%[a-fA-f\\d]{2,2})+)?@)?([\\d\\w][-\\d\\w]{0,253}[\\d\\w]\\.)+[\\w]{2,4}(:[\\d]+)?(/([-+_~.\\d\\w]|%[a-fA-f\\d]{2,2})*)*(\\?(&?([-+_~.\\d\\w]|%[a-fA-f\\d]{2,2})=?)*)?(#([-+_~.\\d\\w]|%[a-fA-f\\d]{2,2})*)?";
		if(Pattern.matches(regex, url)){
			return true;
		}
		return false;
	}
	
	/**
	 * 下载URL的内容到文件
	 * 
	 * @param url
	 * @param fileName
	 */
	public static void downUrlToFile(Activity act, String url, String fileName) {
		String[] allowedContentTypes = new String[] { "text/html;charset=utf-8",
				"image/png", "image/jpeg" };
		client.get(url, new MyBinaryHttpResponseHandler(allowedContentTypes, act,
				fileName));
	}

	static class MyBinaryHttpResponseHandler extends BinaryHttpResponseHandler {
		private Activity act;
		private String fileName;

		public MyBinaryHttpResponseHandler(String[] allowedContentTypes,Activity act,
				String fileName) {
			super(allowedContentTypes);
			this.act = act;
			this.fileName = fileName;
		}

		@Override
		public void onSuccess(byte[] fileData) {
			CacheUtil.writeExternalFile(fileName, fileData);
			System.out.println();
			Toast.makeText(act, "down ok, size: " + fileData.length, Toast.LENGTH_LONG).show();
		}

		@Override
		public void onFailure(Throwable e) {
			Toast.makeText(act, "down fail " + e, Toast.LENGTH_LONG).show();
		}

		@Override
		public void onFailure(Throwable e, String response) {
			Toast.makeText(act, "down fail " + e + "\r\n" + response, Toast.LENGTH_LONG).show();
		}
	}
}
