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
		// 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
		// mFactory);
		// 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
		helper = new DBHelper(context);
		db = helper.getWritableDatabase();
	}

	/**
	 * 插入一条数据
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
	public void updateTime(CacheObject obj) {
		ContentValues cv = new ContentValues();
		cv.put("createTime", obj.getCreateTime());
		db.update("CacheObject", cv, "uid = ?", new String[] { obj.getUid() });
	}

	/**
	 * 更新useCount
	 * 
	 * @param obj
	 */
	public void updateUseCount(CacheObject obj) {
		ContentValues cv = new ContentValues();
		cv.put("useCount", obj.getUseCount());
		db.update("CacheObject", cv, "uid = ?", new String[] { obj.getUid() });
	}

	/**
	 * 删除一条记录
	 * 
	 * @param obj
	 */
	public void delete(CacheObject obj) {
		db.delete("cacheinfo", "uid = ?", new String[] { obj.getUid() });
	}

	/**
	 * 删除一条记录
	 * 
	 * @param obj
	 */
	public CacheObject queryByUrl(String url) {
		CacheObject obj = null;
		Cursor c = db.query("cacheinfo", null, "url = ?", new String[] { url },
				null, null, null);
		if (c.moveToNext()) {
			obj = getCacheObject(c);
			// 来自数据库缓存
			obj.setComeFromCache(true);
		}
		return obj;
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
		return obj;
	}

	/**
	 * 关闭数据库
	 */
	public void closeDB() {
		db.close();
	}
}
