package com.lib.sqlite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

public class SqliteManager {
	private static final String DEFAULT = "Default";
	private static final Map<String, SqliteHandler> DBs = new HashMap<String, SqliteHandler>();
	private static Context mContext;
	private static List<Class<? extends Object>> mObjs;

	/**
	 * 
	 * @param context  application
	 * @param objs  本应用中所有存到数据库中的 bean
	 */
	public static void init(Context context, List<Class<? extends Object>> objs) {
		mContext = context;
		mObjs = objs;
	}

	public static <T> SqliteHandler getHandle(String name) {
		if (mObjs == null) {
			throw new RuntimeException("请在 application 中调用 init");
		}
		SqliteHandler cur = DBs.get(name);
		if (cur == null) {
			cur = new SqliteHandler.Builder(mContext, mObjs, mContext.getDatabasePath(name)).build();
			DBs.put(name, cur);
			return cur;
		} else {
			return cur;
		}
	}

	public static <T> SqliteHandler getHandle() {
		return getHandle(DEFAULT);
	}

}
