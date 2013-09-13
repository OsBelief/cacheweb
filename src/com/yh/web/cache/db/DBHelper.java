package com.yh.web.cache.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "cacheinfo.db";
	private static final int DATABASE_VERSION = 1;

	public DBHelper(Context context) {
		// CursorFactory����Ϊnull,ʹ��Ĭ��ֵ
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * ���ݿ��һ�α�����ʱonCreate�ᱻ����
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS [cacheinfo] ("
				+ "[uid] [CHAR(32)],   [url] TEXT,   [host] [VARCHAR(100)],   [type] [VARCHAR(10)],"
				+ "[mime] [VARCHAR(20)],   [fileName] [VARCHAR(200)],   [createTime] INT64, "
				+ "[cachePolicy] [int(8)],   [useCount] INT DEFAULT 0) ");
	}

	
	/**
	 * ���ݿ�汾����ʱ���¼�
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// ���DATABASE_VERSIONֵ����Ϊ2,ϵͳ�����������ݿ�汾��ͬ,�������onUpgrade
		//db.execSQL("ALTER TABLE person ADD COLUMN other STRING");
	}
}
