package com.lib.sqlite;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

public interface CursorParser {
	/**
	 * 解析cursor为list中的数据
	 * @param tyc 转换器
	 * @param listBean  所有存到数据库中的bean
	 * @param cursor游标
	 * @param mCache缓存
	 * @param cls字节码
	 * @return   T
	 */
	<T> ArrayList<T> parseCursor(TypeConVert tyc, List<Class<? extends Object>> listBean, Cursor cursor, CacheSupport mCache, Class<T> cls);

	<T> ArrayList<T> parseCursor(TypeConVert tyc, Cursor cursor, Class<T> cls, String... column);

}
