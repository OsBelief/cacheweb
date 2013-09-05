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
			res = getImage(act, url, obj.getFileName(), obj.getMime(), null);
		} else if (obj.getMime().equals("text/html")) {
			// HTML ����
			res = getHtml(act, url, obj.getFileName(), obj.getMime(), null);
		} else if (obj.getMime().equals("application/x-javascript")) {
			// JS ����
			//getDefaultInfo(act, url, obj.getFileName(), obj.getMime(), null);
		} else if (obj.getMime().equals("text/css")) {
			// CSS ����
			//getDefaultInfo(act, url, obj.getFileName(), obj.getMime(), null);
		} else if (MIME.fileTypes.contains(obj.getType())) {
			// �ļ�����
			
		} else if (obj.getMime().equals("none")) {
			// û�ҵ�MIME
			
		}
		
		return res;
	}
	
	/**
	 * ��ȡĬ����Ϣ
	 * @param url
	 * @param fileName
	 * @param mime
	 * @param encoding
	 * @return
	 */
	public static WebResourceResponse getDefaultInfo(Activity act, String url, String fileName,
			String mime, String encoding) {
		// ��ȡ����
		InputStream is = IOUtil.readExternalFile(fileName);
		if (is != null) {
			System.out.println("Come From Cache: " + url);
		}else{
			HttpUtil.downUrlToFile(null, url, fileName);
			return null;
		}
		return IOUtil.generateResource(mime, encoding, is);
	}

	public static WebResourceResponse getHtml(Activity act, String url, String fileName,
			String mime, String encoding) {
		return getDefaultInfo(act, url, fileName, mime, encoding);
	}

	/**
	 * ��ȡͼƬ����
	 * 
	 * @param resources
	 * @param url
	 * @param encoding
	 * @return
	 */
	public static WebResourceResponse getImage(Activity act, String url, String fileName,
			String mime, String encoding) {
		return getDefaultInfo(act, url, fileName, mime, encoding);
	}
}
