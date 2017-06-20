package com.lib.sqlite;

import android.database.Cursor;

import java.lang.reflect.Type;
import java.util.ArrayList;

@SuppressWarnings("unused")
public interface CursorParser {
    /**
     * 解析cursor为list中的一个类
     *
     * @param tyc    转换器
     * @param cursor 游标
     * @param mCache 缓存
     * @param cls    字节码
     * @return ArrayList<T>  实体类
     */
    <T> ArrayList<T> parseObject(DataConvert tyc, Cursor cursor, CacheSupport mCache, Class<T> cls);

    /**
     * 解析数据为 list 中的基本类型和 string
     *
     * @param tyc    转换器
     * @param cursor 游标
     * @param cls    8种基本类型或者 string
     * @return ArrayList<T>  基本数据类型或 String
     */
    <T> ArrayList<T> parseColumn(DataConvert tyc, Cursor cursor, Type cls, String columnName);

}
