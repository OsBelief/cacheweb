package com.cache.resource;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

/**
 * 处理静态资源
 * @author Belief
 */
public class CacheResource {
	public static HashSet<String> urlSet = new HashSet<String>();
	//将静态资源的url写入文件中
	public static void writeStaticUrl(HashSet<String> url) {
		FileWriter fw;
		try {
			fw = new FileWriter("E://url.txt");
			for (String string : url) {
				fw.write(string);
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
