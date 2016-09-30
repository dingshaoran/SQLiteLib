package com.lib.sqlite.sqlbuilder;

import android.database.Cursor;

import com.lib.sqlite.CacheSupport;
import com.lib.sqlite.CursorParser;
import com.lib.sqlite.DataConvert;
import com.lib.sqlite.SqliteManager;
import com.lib.utils.LogUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class SqliteCursorParser implements CursorParser {

    @SuppressWarnings("unchecked")
    @Override
    public <T> ArrayList<T> parseObject(DataConvert tyc, Cursor cursor, CacheSupport mCache, Class<T> cls) {
        ArrayList<T> list = new ArrayList<T>();
        if (cursor != null) {
            try {
                Field[] fields = mCache.getFieldWithClass(cls);
                while (cursor.moveToNext()) {
                    list.add(tyc.getObjectValue(cursor, fields, cls));
                }
            } catch (Exception e) {
                LogUtils.e(SqliteManager.TAG, e);
            } finally {
                cursor.close();
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ArrayList<T> parseColumn(DataConvert tyc, Cursor cursor, Class<T> cls) {
        ArrayList<T> list = new ArrayList<T>();
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    list.add((T) tyc.getColumnValue(cursor, cls, 0));
                }

            } catch (Exception e) {
                LogUtils.e(SqliteManager.TAG, e);
            } finally {
                cursor.close();
            }
        }
        return list;
    }
}
