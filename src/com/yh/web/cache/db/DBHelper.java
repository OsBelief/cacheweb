package com.yh.web.cache.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "cacheinfo.db";
	private static final int DATABASE_VERSION = 1;

	public DBHelper(Context context) {
		// CursorFactory设置为null,使用默认值
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
		//db.execSQL("ALTER TABLE person ADD COLUMN other STRING");
	}
}
