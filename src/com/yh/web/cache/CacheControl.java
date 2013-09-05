package com.yh.web.cache;

import java.io.InputStream;

import android.app.Activity;
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
		
		CacheObject obj = new CacheObject(url);
		System.out.println(obj.getType() + " " + obj.getMime() + " | " + url);

		WebResourceResponse res = null;
		if (obj.getMime().startsWith("image")) {
			// ͼƬ����
			res = getImage(url, obj.getMime(), null);
			
		} else if (obj.getMime() == "text/html") {
			// HTML ����
			
		} else if (obj.getMime() == "application/x-javascript") {
			// JS ����
			
		} else if (obj.getMime() == "text/css") {
			// CSS ����
			
		} else if (MIME.fileTypes.contains(obj.getType())) {
			// �ļ�����
			
		} else if (obj.getMime() == "none"){
			// û�ҵ�MIME
			
		}
		return res;
	}

	public static WebResourceResponse getHtml(String url, String encoding) {
		// �˴�д��ȡhtml����ķ���
		String html = url;

		String mime = "text/html";
		InputStream is = IOUtil.getInputStreamFromString(html, encoding);
		return IOUtil.generateResource(mime, encoding, is);
	}
	
	/**
	 * ��ȡͼƬ����
	 * @param resources
	 * @param url
	 * @param encoding
	 * @return
	 */
	public static WebResourceResponse getImage(String url, String mime,
			String encoding) {
		// ��ȡ����·��
		String fileName = CacheObject.getCacheFileName(url, mime);
		InputStream is = IOUtil.readExternalFile(fileName);
		if (is != null){
			System.out.println("Come From Cache: " + url);
		}
		return IOUtil.generateResource(mime, encoding, is);
	}
}
