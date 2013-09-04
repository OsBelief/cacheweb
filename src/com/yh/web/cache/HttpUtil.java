package com.yh.web.cache;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;

/**
 * 
 * @author gudh �漰HTTP�Ĵ����첽����
 */
public class HttpUtil {
	private static AsyncHttpClient client = new AsyncHttpClient();

	/**
	 * ����URL�����ݵ��ļ�
	 * 
	 * @param url
	 * @param fileName
	 */
	public static void downUrlToFile(String url, String fileName) {
		String[] allowedContentTypes = new String[] { "text/html;charset=utf-8",
				"image/png", "image/jpeg" };
		client.get(url, new MyBinaryHttpResponseHandler(allowedContentTypes,
				fileName));
	}

	static class MyBinaryHttpResponseHandler extends BinaryHttpResponseHandler {
		private String fileName;

		public MyBinaryHttpResponseHandler(String[] allowedContentTypes,
				String fileName) {
			super(allowedContentTypes);
			this.fileName = fileName;
		}

		@Override
		public void onSuccess(byte[] fileData) {
			CacheUtil.writeExternalFile(fileName, fileData);
			System.out.println("down ok, size: " + fileData.length);
		}

		@Override
		public void onFailure(Throwable e) {
			System.out.println("down fail " + e);
		}

		@Override
		public void onFailure(Throwable e, String response) {
			System.out.println("down fail " + e + "\r\n" + response);
		}
	}
}
