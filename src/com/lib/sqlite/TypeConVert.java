package com.lib.sqlite;

import java.lang.reflect.Field;

import android.database.Cursor;

public interface TypeConVert {
	/**
	 * 通过cursor获取object
	 * @param cur
	 * @param fields
	 * @param cls
	 * @return
	 */
	public <T> T getObjectValue(Cursor cur, Field[] fields, Class<T> cls);

	/**
	 * 通过corsor获取固定列的值，其他列不赋值
	 * @param cur
	 * @param cls
	 * @param index
	 * @return
	 */
	public <T> Object getColumnValue(Cursor cur, Class<T> cls, int index);

	/**
	 * 获取object中所有的字段值，按照field[]的顺序
	 * @param fields
	 * @param cls
	 * @return
	 */
	String[] getValues(Field[] fields, Object cls);

	/**
	 * 获取object中所有字段的值按照field[]的顺序返回，并在所有的值后面添加query
	 * @param fields
	 * @param cls
	 * @param query
	 * @return
	 */
	String[] getValues(Field[] fields, Object cls, String[] query);
}
