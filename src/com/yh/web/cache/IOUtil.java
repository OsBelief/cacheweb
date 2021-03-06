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
import android.util.Log;
import android.webkit.WebResourceResponse;

/**
 * @author gudh IO处理，包括内部文件，外部文件，key-value等
 */
public class IOUtil {

	/**
	 * 写KeyValue数据库
	 * 
	 * @param act
	 * @param key
	 * @param value
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
	 * 写boolean值到KeyValue数据库
	 * 
	 * @param act
	 * @param key
	 * @param value
	 */
	public static void writeBooleanKeyValue(Activity act, String key,
			boolean value) {
		SharedPreferences sharedPref = act.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	/**
	 * 从KeyValue数据库读boolean值
	 * 
	 * @param act
	 * @param id
	 * @param defaultValue
	 * @return
	 */
	public static boolean readBooleanKeyValue(Activity act, String id,
			boolean defaultValue) {
		SharedPreferences sharedPref = act.getPreferences(Context.MODE_PRIVATE);
		return sharedPref.getBoolean(id, defaultValue);
	}

	/**
	 * 写入内部文件
	 * 
	 * @param act
	 * @param fileName
	 * @param bytes
	 */
	public static void writeInternalFile(Context act, String fileName,
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
		try {
			FileInputStream os = act.openFileInput(fileName);
			return os;
		} catch (FileNotFoundException e) {
			Log.e("readInternalFile", e.getMessage());
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
			File file = new File(fileName);
			File parent = file.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
				do{
					Thread.sleep(2);
				}
				while(!parent.exists());
			}
			FileOutputStream outputStream = new FileOutputStream(file);
			outputStream.write(bytes);
			outputStream.close();
		} catch (Exception e) {
			Log.e("writeExternalFile", e.getMessage() + " " + fileName);
		}
	}
	
	/**
	 * 写流
	 * @param fileName
	 * @param open
	 */
	public static void writeExternalFile(String fileName, InputStream is) {
		try {
			File file = new File(fileName);
			File parent = file.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
				do{
					Thread.sleep(2);
				}
				while(!parent.exists());
			}
			FileOutputStream outputStream = new FileOutputStream(file);
			byte[] bytes = new byte[2048];
			int len = -1;
			while((len = is.read(bytes)) != -1){
				outputStream.write(bytes, 0, len);
			}
			is.close();
			outputStream.close();
		} catch (Exception e) {
			Log.e("writeExternalFile", e.getMessage() + " " + fileName);
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
	 * 以默认utf-8编码读取数据
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static String readStream(InputStream in) throws IOException {
		if(in == null){
			return null;
		}
		return readStream(in, "utf-8");
	}

	/**
	 * 从流中读取文本
	 * 
	 * @param in
	 * @param charSet
	 * @return
	 * @throws IOException
	 */
	public static String readStream(InputStream in, String charSet)
			throws IOException {
		InputStreamReader reader = new InputStreamReader(in, charSet);
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

	public static void writeFileBytes(Context activity, String fileName, byte[] fileData){
		// 以/开头写外部文件，否则写内部文件
		if(fileName.startsWith("/")){
			IOUtil.writeExternalFile(fileName, fileData);
		} else {
			IOUtil.writeInternalFile(activity, fileName, fileData);
		}
	}

	/**
	 * 读取文件流，自动判断是内外文件
	 * 
	 * @param activity
	 * @param fileName
	 * @return
	 */
	public static InputStream getFileInputStream(Activity activity, String fileName){
		if(fileName.startsWith("/")){
			return IOUtil.readExternalFile(fileName);
		} else if(fileName.contains("/")){
			InputStream is = null;
			try {
				// is = activity.getAssets().open(fileName);
				is = IOUtil.readExternalFile(CacheObject.rootPath + fileName);
			} catch (Exception e) {
				e.printStackTrace();
			} 
			Log.d("getInputStream", "Come from inner:" + fileName + " InputStream: " + (is == null ? "not Exist" : "Exist"));
			return is;
		}
		return IOUtil.readInternalFile(activity, fileName);
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
	
	/**
	 * 删除文件
	 * @param fileName
	 */
	public static void deleteFile(String fileName){
		File f = new File(fileName);
		try{
			f.deleteOnExit();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
