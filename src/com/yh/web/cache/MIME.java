package com.yh.web.cache;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.res.AssetManager;

/**
 * @author gudh �洢MIME������Ϣ
 */
public class MIME {

	private static String mimesFileName = "mimes.txt";
	private static String cacheMimeFileName = "cachemime.txt";

	/**
	 * ���е�MIME��Ϣ�����ļ�
	 */
	private static HashMap<String, String> mimeMaps = new HashMap<String, String>();

	/**
	 * ��Ҫ�����MIME��Ϣ�����ļ�
	 */
	private static Set<String> cacheMimeSet = new HashSet<String>();

	public final static String defaultType = "html";
	public final static String noneType = "none";

	public static Set<String> fileTypes = new HashSet<String>();

	/**
	 * ��ʼ��MIME��Ϣ���������ļ��ж�ȡ
	 * 
	 * @param act
	 */
	public static void initMIME(Activity act) {
		AssetManager assets = act.getAssets();

		try {
			for (String s : assets.list("")) {
				System.out.println("assFile:" + s);
			}
			System.out.println(mimesFileName);
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
			in = assets.open(cacheMimeFileName);
			if (in != null) {
				txt = IOUtil.readStream(in);
				lines = txt.split("\r\n");
				for (String line : lines) {
					if (line.equals("")) {
						continue;
					}
					// ��ȡ�ļ�����
					if (line.startsWith("fileTypes=")) {
						String infos[] = line.replace("fileTypes=", "").split(
								"\\|");
						for (String info : infos) {
							if (!info.equals("")) {
								fileTypes.add(info);
							}
						}
					}else{
						cacheMimeSet.add(line);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ȡ������Ҫ������ļ�����
	 * 
	 * @return
	 */
	public static Set<String> getCacheMimes() {
		return cacheMimeSet;
	}

	/**
	 * ��ȡָ�����͵�MIME
	 * 
	 * @param type
	 * @return
	 */
	public static String getMimeFromType(String type) {
		String mime = mimeMaps.get(type);
		if(mime == null){
			mime = "none";
		}
		return mime;
	}
}
