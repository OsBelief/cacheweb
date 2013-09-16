package com.yh.web.cache.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yh.web.cache.CacheObject;

public class CacheOrm {

	private DBHelper helper;
	private SQLiteDatabase db;

	public CacheOrm(Context context) {
		// ��ΪgetWritableDatabase�ڲ�������mContext.openOrCreateDatabase(mName, 0,
		// mFactory);
		// ����Ҫȷ��context�ѳ�ʼ��,���ǿ��԰�ʵ����DBManager�Ĳ������Activity��onCreate��
		helper = new DBHelper(context);
		db = helper.getWritableDatabase();
	}

	/**
	 * ����һ������
	 * 
	 * @param obj
	 */
	public void add(CacheObject obj) {
		db.execSQL(
				"INSERT INTO cacheinfo VALUES(null, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				new Object[] { obj.getUid(), obj.getUrl(), obj.getHost(),
						obj.getType(), obj.getMime(), obj.getFileName(),
						obj.getCreateTime(), obj.getCachePolicy(),
						obj.getUseCount() });
	}

	/**
	 * ��������
	 * 
	 * @param objs
	 */
	public void add(List<CacheObject> objs) {
		db.beginTransaction(); // ��ʼ����
		try {
			for (CacheObject obj : objs) {
				db.execSQL(
						"INSERT INTO cacheinfo VALUES(null, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
						new Object[] { obj.getUid(), obj.getUrl(),
								obj.getHost(), obj.getType(), obj.getMime(),
								obj.getFileName(), obj.getCreateTime(),
								obj.getCachePolicy(), obj.getUseCount() });
			}
			db.setTransactionSuccessful(); // ��������ɹ����
		} finally {
			db.endTransaction(); // ��������
		}
	}

	/**
	 * ����ʱ��
	 * 
	 * @param obj
	 */
	public void updateTime(CacheObject obj) {
		ContentValues cv = new ContentValues();
		cv.put("createTime", obj.getCreateTime());
		db.update("CacheObject", cv, "uid = ?", new String[] { obj.getUid() });
	}

	/**
	 * ����useCount
	 * 
	 * @param obj
	 */
	public void updateUseCount(CacheObject obj) {
		ContentValues cv = new ContentValues();
		cv.put("useCount", obj.getUseCount());
		db.update("CacheObject", cv, "uid = ?", new String[] { obj.getUid() });
	}

	/**
	 * ɾ��һ����¼
	 * 
	 * @param obj
	 */
	public void delete(CacheObject obj) {
		db.delete("cacheinfo", "uid = ?", new String[] { obj.getUid() });
	}

	/**
	 * ɾ��һ����¼
	 * 
	 * @param obj
	 */
	public CacheObject queryByUrl(String url) {
		CacheObject obj = null;
		Cursor c = db.query("cacheinfo", null, "url = ?", new String[] { url },
				null, null, null);
		if (c.moveToNext()) {
			obj = getCacheObject(c);
			// �������ݿ⻺��
			obj.setComeFromCache(true);
		}
		return obj;
	}

	/**
	 * ��ѯ��¼
	 * 
	 * @return
	 */
	public List<CacheObject> query(String sql) {
		ArrayList<CacheObject> objs = new ArrayList<CacheObject>();
		Cursor c = db.rawQuery(sql, null);
		while (c.moveToNext()) {
			CacheObject obj = getCacheObject(c);
			objs.add(obj);
		}
		c.close();
		return objs;
	}

	/**
	 * �ӵ�ǰ�α괦��ȡ���������Ϣ
	 * 
	 * @param c
	 * @return
	 */
	public CacheObject getCacheObject(Cursor c) {
		CacheObject obj = new CacheObject();
		obj.setUid(c.getString(c.getColumnIndex("uid")));
		obj.setUrl(c.getString(c.getColumnIndex("url")));
		obj.setHost(c.getString(c.getColumnIndex("host")));
		obj.setType(c.getString(c.getColumnIndex("type")));
		obj.setMime(c.getString(c.getColumnIndex("mime")));
		obj.setFileName(c.getString(c.getColumnIndex("fileName")));
		obj.setCreateTime(c.getLong(c.getColumnIndex("createTime")));
		obj.setCachePolicy(c.getInt(c.getColumnIndex("cachePolicy")));
		obj.setUseCount(c.getInt(c.getColumnIndex("useCount")));
		return obj;
	}

	/**
	 * �ر����ݿ�
	 */
	public void closeDB() {
		db.close();
	}
}
