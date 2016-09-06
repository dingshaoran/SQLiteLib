package com.lib.sqlite.sqlbuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.lib.sqlite.CacheSupport;
import com.lib.sqlite.CursorParser;
import com.lib.sqlite.TypeConVert;
import com.lib.sqlite.utils.LogUtils;

public class SqliteCursorParser implements CursorParser {

	@SuppressWarnings("unchecked")
	@Override
	public <T> ArrayList<T> parseCursor(TypeConVert tyc, List<Class<? extends Object>> listBean, Cursor cursor, CacheSupport mCache, Class<T> cls) {
		ArrayList<T> list = new ArrayList<T>();
		if (cursor != null) {
			try {
				if (listBean.contains(cls)) {
					Field[] fields = mCache.getFieldWithClass(cls);
					while (cursor.moveToNext()) {
						list.add(tyc.getObjectValue(cursor, fields, cls));
					}
				} else {
					while (cursor.moveToNext()) {
						list.add((T) tyc.getColumnValue(cursor, cls, 0));
					}
				}

			} catch (Exception e) {
				LogUtils.e(e);
			} finally {
				cursor.close();
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ArrayList<T> parseCursor(TypeConVert tyc, Cursor cursor, Class<T> cs, String... column) {
		Field[] fields = new Field[column.length];
		ArrayList<T> array = new ArrayList<T>();
		try {
			for (int i = 0; i < column.length; i++) {
				fields[i] = cs.getField(column[i]);
			}
		} catch (Exception e) {
		}
		if (cursor != null) {
			try {
				while (cursor.moveToNext()) {
					T t = null;
					try {
						t = cs.newInstance();
					} catch (Exception e) {
						Constructor<?> c = cs.getDeclaredConstructors()[0];
						c.setAccessible(true);
						TypeVariable<?>[] typeVariables = c.getTypeParameters();
						Object[] ob = new Object[typeVariables.length];
						for (int i = 0; i < ob.length; i++) {
							ob[i] = typeVariables[i].getGenericDeclaration();
						}
						try {
							t = (T) c.newInstance(ob);
						} catch (Exception e1) {
						}
					}
					for (int i = 0; i < column.length; i++) {
						int index = cursor.getColumnIndex(column[i]);
						fields[i].set(t, tyc.getColumnValue(cursor, fields[i].getType(), index));
					}
					array.add(t);
				}
			} catch (Exception e) {
				LogUtils.e(e);
			} finally {
				cursor.close();
			}
		}
		return null;
	}
}
