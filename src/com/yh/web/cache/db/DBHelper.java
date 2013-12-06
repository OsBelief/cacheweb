package com.yh.web.cache.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "cacheinfo.db";
	private static final int DATABASE_VERSION = 1;

	private static Context context;

	private static final String INIT_DB_NAME = "cfile/cacheinfo.db";
	@SuppressLint("SdCardPath")
	private static final String INNER_DB_NAME = "/data/data/cn.yicha.cache.fuli/databases/cacheinfo.db";

	public DBHelper(Context context) {
		// CursorFactory设置为null,使用默认值
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		DBHelper.context = context;
	}

	/**
	 * 数据库第一次被创建时onCreate会被调用
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS [cacheinfo] ("
				+ "[uid] [CHAR(32)],   [url] TEXT,   [host] [VARCHAR(100)],   [type] [VARCHAR(10)],"
				+ "[mime] [VARCHAR(20)],   [fileName] [VARCHAR(200)],   [createTime] INT64, "
				+ "[cachePolicy] [int(8)],   [useCount] INT DEFAULT 0) ");
	}

	/**
	 * 数据库版本更改时的事件
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
		// db.execSQL("ALTER TABLE person ADD COLUMN other STRING");
	}

	public SQLiteDatabase getDB() {
		if (!new File(INNER_DB_NAME).exists()) {
			this.getReadableDatabase();
			try {
				InputStream is = context.getAssets().open(INIT_DB_NAME);
				FileOutputStream fos = new FileOutputStream(INNER_DB_NAME);
				byte[] buffer = new byte[2048];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(INNER_DB_NAME,
				null);
		if (db == null) {
			db = this.getWritableDatabase();
		}
		return db;
	}
}
