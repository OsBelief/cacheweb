package com.yh.web.cache;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.res.AssetManager;

/**
 * @author gudh 存储MIME类型信息
 */
public class MIME {

	private static String mimesFileName = "mimes.txt";
	private static String cacheMimeFileName = "cachemime.txt";

	/**
	 * 所有的MIME信息配置文件
	 */
	private static HashMap<String, String> mimeMaps = new HashMap<String, String>();

	/**
	 * 需要缓存的MIME信息配置文件
	 */
	private static Set<String> cacheMimeSet = new HashSet<String>();

	public final static String defaultType = "html";
	public final static String noneType = "none";

	public static Set<String> fileTypes = new HashSet<String>();

	/**
	 * 初始化MIME信息，从配置文件中读取
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
					// 读取文件类型
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
	 * 获取所有需要缓存的文件类型
	 * 
	 * @return
	 */
	public static Set<String> getCacheMimes() {
		return cacheMimeSet;
	}

	/**
	 * 获取指定类型的MIME
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
