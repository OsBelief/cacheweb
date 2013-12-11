package com.yh.web.cache.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.yh.web.cache.CacheObject;

public class CacheOrm {

	public final static String tableName = "cacheinfo";
	public final static String baseQuery = "select * from cacheinfo ";

	private DBHelper helper;
	private SQLiteDatabase db;

	public CacheOrm(Context context) {
		// 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
		// mFactory);
		// 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
		helper = new DBHelper(context);
		db = helper.getDB();
	}

	/**
	 * 插入一条数据
	 * 
	 * @param obj
	 */
	public void add(CacheObject obj) {
		db.execSQL(
				"INSERT INTO cacheinfo VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)",
				new Object[] { obj.getUid(), obj.getUrl(), obj.getHost(),
						obj.getType(), obj.getMime(), obj.getFileName(),
						obj.getCreateTime(), obj.getCachePolicy(),
						obj.getUseCount() });
	}

	/**
	 * 插入数据
	 * 
	 * @param objs
	 */
	public void add(List<CacheObject> objs) {
		db.beginTransaction(); // 开始事务
		try {
			for (CacheObject obj : objs) {
				db.execSQL(
						"INSERT INTO cacheinfo VALUES(null, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
						new Object[] { obj.getUid(), obj.getUrl(),
								obj.getHost(), obj.getType(), obj.getMime(),
								obj.getFileName(), obj.getCreateTime(),
								obj.getCachePolicy(), obj.getUseCount() });
			}
			db.setTransactionSuccessful(); // 设置事务成功完成
		} finally {
			db.endTransaction(); // 结束事务
		}
	}

	/**
	 * 更新时间
	 * 
	 * @param obj
	 */
	public boolean updateTime(CacheObject obj) {
		ContentValues cv = new ContentValues();
		cv.put("createTime", obj.getCreateTime());
		return db.update(tableName, cv, "uid = ?", new String[] { obj.getUid() }) == 1;
	}

	/**
	 * 更新useCount
	 * 
	 * @param obj
	 */
	public boolean updateUseCount(CacheObject obj) {
		ContentValues cv = new ContentValues();
		cv.put("useCount", obj.getUseCount());
		return db.update(tableName, cv, "uid = ?", new String[] { obj.getUid() }) == 1;
	}

	/**
	 * 删除一条记录
	 * 
	 * @param obj
	 */
	public boolean delete(CacheObject obj) {
		return db.delete(tableName, "uid = ?", new String[] { obj.getUid() }) == 1;
	}

	/**
	 * 通过URL查询
	 * 
	 * @param obj
	 */
	public CacheObject queryByUrl(String url) {
		CacheObject obj = null;
		Cursor c = db.query(tableName, null, "url = ?", new String[] { url },
				null, null, null);
		if (c.moveToNext()) {
			obj = getCacheObject(c);
		}
		return obj;
	}

	/**
	 * 以指定的条件，按createTime排序，限制limit个结果
	 * @param selection
	 * @param selectionArgs
	 * @param limit
	 * @return
	 */
	public List<CacheObject> query(String selection, String[] selectionArgs,
			String limit) {
		List<CacheObject> objs = new ArrayList<CacheObject>();
		Cursor c = db.query(tableName, null, selection, selectionArgs, null, null,
				"createTime", limit);
		while (c.moveToNext()) {
			CacheObject obj = getCacheObject(c);
			objs.add(obj);
		}
		c.close();
		return objs;
	}

	/**
	 * 查询记录
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
	 * 从当前游标处获取缓存对象信息
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

		// 来自数据库缓存
		obj.setComeFromCache(true);
		return obj;
	}

	/**
	 * 关闭数据库
	 */
	public void closeDB() {
		db.close();
	}
	
	/**
	 * 更新数据库
	 * 
	 * @param obj
	 * @param result
	 */
	public void updateDB(CacheObject obj, boolean result) {
		try {
			if (!result) {
				// 失败则删除数据库记录
				if (obj != null && obj.isComeFromCache()) {
					delete(obj);
					Log.i("updateDB", "DELETE | " + obj.getUrl());
				}
			} else {
				// 成功并存在则更新创建时间
				obj.setCreateTime(System.currentTimeMillis());
				if (obj != null && obj.isComeFromCache()) {
					updateTime(obj);
					Log.i("updateDB", "UPTIME | " + obj.getUrl());
				} else {
					add(obj);
					Log.i("updateDB", "INSERT | " + obj.getUrl());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("updateDB", obj.getUrl() + "\t" + e.getMessage());
		}
	}
}
