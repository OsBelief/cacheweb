package com.yh.web.cache;

import java.io.InputStream;

import android.content.Context;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.yh.web.cache.db.CacheOrm;

/**
 * @author gudh ������Ʋ���
 */
public class CacheControl {

	public static CacheOrm orm = null;

	/**
	 * ��ʼ������ORM
	 * 
	 * @param context
	 */
	public static void initCache(Context context) {
		orm = new CacheOrm(context);
	}

	/**
	 * ��׽���󣬿��ƻ������
	 * 
	 * @param context
	 *            ����Web���ڵ�Activity
	 * @param url
	 *            ����ĵ�ַ
	 * @return ����null��Զ��ַ���ǿ���ȡ���ص���Դ
	 */
	public static WebResourceResponse getResource(Context context, WebView web,
			String url) {
		// ��ȡת����URL
		String urlb = url;
		url = HttpUtil.getToUrl(url);
		if (url == null) {
			Log.i("getResource", "DisCache url | " + urlb);
			// ���ת����URLΪnull�����ʾ����Ҫ����
			return null;
		}
		// ��ѯ���ݿ��Ƿ��л���
		CacheObject obj = orm.queryByUrl(url);
		if (obj == null) {
			obj = new CacheObject(url);
			Log.i("getResource",
					"New Fetch | " + obj.getType() + " " + obj.getMime()
							+ " | " + url);
		} else {
			Log.i("getResource",
					"From Cache | " + obj.getType() + " " + obj.getMime()
							+ " | " + url);
		}

		WebResourceResponse res = null;
		if (obj.getMime().startsWith("image")) {
			// ͼƬ����
			res = getImage(context, obj, null);
		} else if (obj.getMime().equals("text/html")) {
			// HTML ����
			res = getHtml(context, obj, null);
		} else if (obj.getMime().equals("application/x-javascript")) {
			// JS ����
			res = getDefaultInfo(context, obj, null);
		} else if (obj.getMime().equals("text/css")) {
			// CSS ����
			res = getDefaultInfo(context, obj, null);
		} else if (CacheFilter.notCacheType.contains(obj.getType())) {
			// �����洦��

		} else if (obj.getMime().equals("none")) {
			// û�ҵ�MIME

		}

		return res;
	}

	/**
	 * ��ȡĬ����Ϣ
	 * 
	 * @param context
	 * @param obj
	 * @param encoding
	 * @return
	 */
	public static WebResourceResponse getDefaultInfo(Context context,
			CacheObject obj, String encoding) {
		// �Ƿ���Ҫ���»���
		boolean needUpdate = false;
		// ���ؽ��
		WebResourceResponse res = null;

		if (obj.isComeFromCache()) {
			// ���Ի���
			if (!obj.isExpire(System.currentTimeMillis())) {
				// ����δ����
				InputStream is = IOUtil.readExternalFile(obj.getFileName());
				if (is != null) {
					// �����ļ���Ȼ����
					res = IOUtil.generateResource(obj.getMime(), encoding, is);
					obj.setUseCount(obj.getUseCount() + 1);
					Log.d("updateDB", "UseCount " + obj.getUseCount() + " | "
							+ obj.getUrl());
					orm.updateUseCount(obj);
				} else {
					Log.i("getDefaultInfo", "File NotExist | " + obj.getUrl()
							+ " " + obj.getFileName());
					needUpdate = true;
				}
			} else {
				Log.i("getDefaultInfo", "Cache Expire | " + obj.getUrl());
				needUpdate = true;
			}
		} else {
			Log.i("getDefaultInfo", "NeedUpdate | " + obj.getUrl());
			needUpdate = true;
		}
		if (needUpdate) {
			// ���»���
			HttpUtil.downUrlToFile(null, obj);
		}
		return res;
	}

	/**
	 * ��ȡhtml����
	 * 
	 * @param context
	 * @param obj
	 * @param encoding
	 * @return
	 */
	public static WebResourceResponse getHtml(Context context, CacheObject obj,
			String encoding) {
		return getDefaultInfo(context, obj, encoding);
	}

	/**
	 * ��ȡͼƬ����
	 * 
	 * @param context
	 * @param obj
	 * @param encoding
	 * @return
	 */
	public static WebResourceResponse getImage(Context context,
			CacheObject obj, String encoding) {
		return getDefaultInfo(context, obj, encoding);
	}
}
