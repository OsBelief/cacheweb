package com.yh.util;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;

import com.yh.web.cache.CacheObject;
import com.yh.web.cache.IOUtil;
import com.yh.web.cache.db.CacheOrm;

public class CacheDBUtil {

	/**
	 * 将assets/urls.txt中的所有url生成数据库记录
	 * @param activity
	 */
	public static void generateSQLite(Activity activity){
		String[] urls = null;
		try {
			String text = IOUtil.readStream(activity.getAssets().open("urls.txt"));
			urls = text.split("\n");
			long s = System.currentTimeMillis();
			generateSQLite(activity, urls);
			long t = System.currentTimeMillis() - s;
			System.out.println("GenerateSQLite use Time:" + t);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 对所有URL进行处理
	 * @param activity
	 * @param urls
	 */
	private static void generateSQLite(Activity activity, String[] urls){
		CacheOrm orm = new CacheOrm(activity);
		for(String url : urls){
			url = url.trim();
			if(!url.startsWith("http") || url.length() < 10){
				continue;
			}
			CacheObject obj = new CacheObject(url);
			obj.setFileName(getFileName(url));
			System.out.println(url + " " + obj.getFileName());
			obj.setCreateTime(System.currentTimeMillis());
			orm.add(obj);
		}
	}
	
	/**
	 * 根据URL获取相对文件路径
	 * @param url 
	 * @param rootPath 路径前缀
	 * @return
	 */
	private static String getFileName(String url){
		String id = MD5Util.digestString(url);
		String fileName = new StringBuffer().append("cfile/")
				.append(id.substring(0, 1)).append("/")
				.append(id.substring(10)).toString();
		return fileName;
	}

	private static class MD5Util {
		private static MessageDigest md = null;
		static {
			try {
				md = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 获取字符串的md5
		 * 
		 * @param str
		 * @return
		 */
		public synchronized static String digestString(String str) {
			md.reset();
			md.update(str.getBytes());
			byte[] digest = md.digest();
			return bin2Hex(digest);
		}

		/**
		 * 将buf转为字符串
		 * 
		 * @param buff
		 * @return
		 */
		synchronized static String bin2Hex(byte[] buff) {
			StringBuilder sb = new StringBuilder();
			String temp;
			for (byte bt : buff) {
				temp = Integer.toHexString(bt & 0xff);
				sb.append(temp.length() == 1 ? ('0' + temp) : temp);
			}
			return sb.toString();
		}
	}
}
