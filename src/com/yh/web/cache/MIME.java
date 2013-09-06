package com.yh.web.cache;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.content.res.AssetManager;

/**
 * @author gudh
 * 
 *         �洢MIME������Ϣ
 */
public class MIME {

	private static String mimesFileName = "mimes.txt";

	/**
	 * ���е�MIME��Ϣ�����ļ�
	 */
	private static HashMap<String, String> mimeMaps = new HashMap<String, String>();

	public final static String defaultType = "html";
	public final static String noneType = "none";

	/**
	 * ��ʼ��MIME��Ϣ���������ļ��ж�ȡ
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
	 * ��ȡָ�����͵�MIME
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
