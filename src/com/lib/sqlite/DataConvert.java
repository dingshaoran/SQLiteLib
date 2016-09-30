package com.lib.sqlite;

import android.database.Cursor;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public interface DataConvert {
    /**
     * 通过cursor获取object
     */
    <T> T getObjectValue(Cursor cur, Field[] fields, Class<T> cls);

    /**
     * 通过corsor获取固定列的值，其他列不赋值
     */
    <T> Object getColumnValue(Cursor cur, Type cls, int index);

    /**
     * 获取object中所有的字段值，按照field[]的顺序
     */
    String[] getValues(Field[] fields, Object cls);

    /**
     * 获取object中所有字段的值按照field[]的顺序返回，并在所有的值后面添加query
     */
    String[] getValues(Field[] fields, Object cls, String[] query);

    /**
     * 获取object中所有字段的值按照field[]的顺序返回，并在所有的值前面添加 query信息替换对应 pos 的值
     */
    String[] getPutValues(String[] query, int pos, Field[] fields, Object cls);
}
