package com.yh.web.cache;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.app.Activity;
import android.util.Log;

/**
 * @author gudh
 * 
 *         存储MIME类型信息
 */
public class MIME {

	public final static String MIME_NAME = "mimes.txt";

	/**
	 * 所有的MIME信息配置文件
	 */
	private static HashMap<String, String> mimeMaps;

	public final static String DEFAULT_TYPE = "html";
	public final static String NONE_TYPE = "none";

	/**
	 * 初始化MIME信息，从配置文件中读取
	 * 
	 * @param act
	 */
	public static void initMIME(Activity act) {
		try {
			// 先从外部读文件，如果没有，读取asset的配置
			InputStream in = IOUtil.readInternalFile(act, MIME_NAME);
			if (in == null) {
				in = act.getAssets().open(MIME_NAME);
				Log.i("initMIME", "init from asset");
			} else{
				Log.i("initMIME", "init from internal file");
			}
			String txt = IOUtil.readStream(in).trim();
			initMIME(txt);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean initMIME(String txt) {
		boolean res = false;
		try {
			String lines[] = txt.split("\n");
			HashMap<String, String> mimeMapsNew = new HashMap<String, String>(lines.length);
			for (String line : lines) {
				String[] infos = line.trim().split("\t");
				if (infos.length == 2) {
					mimeMapsNew.put(infos[0], infos[1]);
				}
			}
			if(mimeMapsNew.size() > 0){
				// 切换
				mimeMaps = mimeMapsNew;
				mimeMapsNew = null;
			}
			res = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * 获取指定类型的MIME
	 * 
	 * @param type
	 * @return
	 */
	public static String getMimeFromType(String type) {
		String mime = mimeMaps.get(type);
		if (mime == null) {
			mime = "none";
		}
		return mime;
	}
}
