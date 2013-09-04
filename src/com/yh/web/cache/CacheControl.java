package com.yh.web.cache;

import java.io.InputStream;

import com.yh.web.R;

import android.app.Activity;
import android.content.res.Resources;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

/**
 * @author gudh ������Ʋ���
 */
public class CacheControl {

	/**
	 * ��׽���󣬿��ƻ������
	 * 
	 * @param act
	 *            ����Web���ڵ�Activity
	 * @param url
	 *            ����ĵ�ַ
	 * @return ����null��Զ��ַ���ǿ���ȡ���ص���Դ
	 */
	public static WebResourceResponse getResource(Activity act, WebView web,
			String url) {
		WebResourceResponse res = null;
		if (url.endsWith(".png") || url.endsWith(".jpg")) {
			System.out.println("##image: " + url);
			res = getImage(act.getResources(), url, null);
		} else if (url.contains(".html")) {
			System.out.println("##html: " + url);
		} else if (url.contains(".js")) {
			System.out.println("##js: " + url);
		} else if (url.contains(".css")) {
			System.out.println("##css: " + url);
		} else{
			System.out.println("##other: " + url);
		}
		return res;
	}

	public static WebResourceResponse getHtml(String url, String encoding) {
		// �˴�д��ȡhtml����ķ���
		String html = url;

		String mime = "text/html";
		InputStream is = CacheUtil.getInputStreamFromString(html, encoding);
		return CacheUtil.generateResource(mime, encoding, is);
	}

	public static WebResourceResponse getImage(Resources resources, String url,
			String encoding) {
		// �˴�д��ȡhtml����ķ���
		String mime = "image/png";
		InputStream is = CacheUtil.getInputStreamFromID(resources,
				R.drawable.ic_launcher);
		// is ��Ϊ�տ��Ը���Ϊ�Լ���Ҫ�滻������
		is = null;
		return CacheUtil.generateResource(mime, encoding, is);
	}
}
