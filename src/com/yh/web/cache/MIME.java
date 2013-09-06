package com.yh.web.cache;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.content.res.AssetManager;

/**
 * @author gudh
 * 
 *         存储MIME类型信息
 */
public class MIME {

	private static String mimesFileName = "mimes.txt";

	/**
	 * 所有的MIME信息配置文件
	 */
	private static HashMap<String, String> mimeMaps = new HashMap<String, String>();

	public final static String defaultType = "html";
	public final static String noneType = "none";

	/**
	 * 初始化MIME信息，从配置文件中读取
	 * 
	 * @param act
	 */
	public static void initMIME(AssetManager assets) {
		try {
			InputStream in = assets.open(mimesFileName);
			String txt;
			String lines[];
			if (in != null) {
				txt = IOUtil.readStream(in);
				lines = txt.split("\r\n");
				for (String line : lines) {
					String[] infos = line.split("\t");
					if (infos.length == 2) {
						mimeMaps.put(infos[0], infos[1]);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
