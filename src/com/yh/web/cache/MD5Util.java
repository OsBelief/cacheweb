package com.yh.web.cache;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * md5�Ļ�ȡ�������ļ�·������
 * 
 * @author yh ����ʱ�䣺2012-4-19
 */
public class MD5Util {

	private static MessageDigest md = null;
	private static int BUFF_SIZE = 1024 * 4;

	static {
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��һ����ַ��ȡ��Ӧmd5����·��
	 * 
	 * @param url
	 * @return
	 */
	public synchronized static String getFileName(String url) {
		String m = digestString(url);
		m = m.substring(0, 2) + "/" + m.substring(2, 4) + "/" + m.substring(4);
		return m;
	}

	/**
	 * ��ȡbuff��md5
	 * 
	 * @param buff
	 * @return
	 */
	public synchronized static String digest(byte[] buff) {
		md.reset();
		md.update(buff);
		byte[] digest = md.digest();
		return bin2Hex(digest);
	}

	/**
	 * ��ȡ�ļ����ݵ�md5
	 * 
	 * @param filePath
	 *            �ļ�·��
	 * @return
	 * @throws Exception
	 */
	public synchronized static String digestFile(String filePath)
			throws Exception {
		md.reset();
		InputStream in = new BufferedInputStream(new FileInputStream(filePath));
		byte[] buff = new byte[BUFF_SIZE];
		int length = in.read(buff);
		while (length > 0) {
			md.update(buff, 0, length);
			length = in.read(buff);
		}
		in.close();
		buff = md.digest();
		return bin2Hex(buff);
	}

	/**
	 * ��ȡ�ַ�����md5
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
	 * ��bufתΪ�ַ���
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
