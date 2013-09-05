package com.yh.web.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.webkit.WebResourceResponse;

/**
 * @author gudh 缓存Util
 */
public class IOUtil {

	/**
	 * 写KeyValue数据库
	 * 
	 * @param fileName
	 * @param os
	 */
	public static void writeKeyValue(Activity act, String key, String value) {
		SharedPreferences sharedPref = act.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	/**
	 * 读KeyValue数据库
	 * 
	 * @param act
	 * @param id
	 * @param defaultValue
	 * @return
	 */
	public static String readKeyValue(Activity act, String id,
			String defaultValue) {
		SharedPreferences sharedPref = act.getPreferences(Context.MODE_PRIVATE);
		return sharedPref.getString(id, defaultValue);
	}

	/**
	 * 写入内部文件
	 * 
	 * @param act
	 * @param fileName
	 * @param bytes
	 */
	public static void writeInternalFile(Activity act, String fileName,
			byte[] bytes) {
		try {
			FileOutputStream outputStream = act.openFileOutput(fileName,
					Context.MODE_PRIVATE);
			outputStream.write(bytes);
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从内部文件获取输入流
	 * 
	 * @param act
	 * @param fileName
	 * @return
	 */
	public static InputStream readInternalFile(Activity act, String fileName) {
		if (new File(fileName).exists()) {
			try {
				FileInputStream os = act.openFileInput(fileName);
				return os;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 写入外部文件
	 * 
	 * @param fileName
	 * @param bytes
	 */
	public static void writeExternalFile(String fileName, byte[] bytes) {
		try {
			File parent = new File(fileName).getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			FileOutputStream outputStream = new FileOutputStream(fileName);
			outputStream.write(bytes);
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从外部文件获取输入流
	 * 
	 * @param fileName
	 * @return
	 */
	public static InputStream readExternalFile(String fileName) {
		if (fileName != null && new File(fileName).exists()) {
			try {
				return new FileInputStream(fileName);
			} catch (FileNotFoundException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * 从字符串获取字节流
	 * 
	 * @param text
	 * @param charSet
	 *            给定字符串的编码，如果出错则默认utf-8
	 * @return
	 */
	public static InputStream getInputStreamFromString(String text,
			String charSet) {
		if (text != null) {
			try {
				return new ByteArrayInputStream(text.getBytes(charSet));
			} catch (UnsupportedEncodingException e) {
				// 如果失败则返回utf-8
				try {
					return new ByteArrayInputStream(text.getBytes("utf-8"));
				} catch (UnsupportedEncodingException e1) {
				}
			}
		}
		return null;
	}

	/**
	 * 从Drawable对像获取InputDream
	 * 
	 * @param drawObj
	 * @param mime
	 *            格式，若含png则为png，否则都将转成jpeg格式
	 * @return
	 */
	public static InputStream getInputStreamFromDrawable(Drawable drawObj,
			String mime) {
		if (drawObj != null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			BitmapDrawable bDrawObj = (BitmapDrawable) drawObj;
			// 获取输入流格式
			CompressFormat cFormat = null;
			if (mime != null
					&& mime.toLowerCase(Locale.getDefault()).contains("png")) {
				cFormat = Bitmap.CompressFormat.PNG;
			} else {
				cFormat = Bitmap.CompressFormat.JPEG;
			}
			bDrawObj.getBitmap().compress(cFormat, 100, stream);
			return new ByteArrayInputStream(stream.toByteArray());
		}
		return null;
	}

	/**
	 * 根据Id获取输入流
	 * 
	 * @param resources
	 * @param id
	 * @return
	 */
	public static InputStream getInputStreamFromID(Resources resources, int id) {
		if (resources != null) {
			return resources.openRawResource(id);
		}
		return null;
	}

	/**
	 * 从流中读取文本
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static String readStream(InputStream in) throws IOException {
		InputStreamReader reader = new InputStreamReader(in, "utf-8");
		StringBuffer sb = new StringBuffer();
		int len = -1;
		char[] buf = new char[2048];
		while ((len = reader.read(buf)) != -1) {
			sb.append(buf, 0, len);
		}
		reader.close();
		in.close();
		return sb.toString();
	}

	/**
	 * 产生WebResourceResponse
	 * 
	 * @param mime
	 * @param encoding
	 *            默认是utf-8
	 * @param is
	 * @return
	 */
	public static WebResourceResponse generateResource(String mime,
			String encoding, InputStream is) {
		if (is == null) {
			return null;
		}
		if (encoding == null) {
			encoding = "utf-8";
		}
		return new WebResourceResponse(mime, encoding, is);
	}
}
