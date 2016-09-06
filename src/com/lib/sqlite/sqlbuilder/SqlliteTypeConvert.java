package com.lib.sqlite.sqlbuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.TypeVariable;

import android.database.Cursor;

import com.lib.sqlite.TypeConVert;
import com.lib.sqlite.utils.LogUtils;

public class SqlliteTypeConvert implements TypeConVert {
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObjectValue(Cursor cur, Field[] fields, Class<T> cs) {
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
		for (int i = 0; i < fields.length; i++) {
			try {
				Class<?> cls = fields[i].getType();
				String si = cur.getString(i);
				if (cls == String.class) {
					fields[i].set(t, si);
				} else if (cls == int.class || cls == Integer.class) {
					fields[i].set(t, Integer.valueOf(si));
				} else if (cls == long.class || cls == Long.class) {
					fields[i].set(t, Long.valueOf(si));
				} else if (cls == double.class || cls == Double.class) {
					fields[i].set(t, Double.valueOf(si));
				} else if (cls == float.class || cls == Float.class) {
					fields[i].set(t, Float.valueOf(si));
				} else if (cls == short.class || cls == Short.class) {
					fields[i].set(t, Short.valueOf(si));
				} else if (cls == byte.class || cls == Byte.class) {
					fields[i].set(t, Byte.valueOf(si));
				} else if (cls == boolean.class || cls == Boolean.class) {
					fields[i].set(t, Boolean.valueOf(si));
				} else if (cls == char.class || cls == Character.class) {
					fields[i].set(t, si == null ? "" : si.charAt(0));
				} else {//不是基本类型是obj
				}
			} catch (Exception e) {
			}
		}
		return t;
	}

	@Override
	public <T> Object getColumnValue(Cursor cur, Class<T> cls, int index) {
		if (cls == String.class) {
			return cur.getString(index);
		} else if (cls == int.class || cls == Integer.class) {
			return cur.getInt(index);
		} else if (cls == long.class || cls == Long.class) {
			return cur.getLong(index);
		} else if (cls == double.class || cls == Double.class) {
			return cur.getDouble(index);
		} else if (cls == float.class || cls == Float.class) {
			return cur.getFloat(index);
		} else if (cls == short.class || cls == Short.class) {
			return cur.getShort(index);
		} else if (cls == byte.class || cls == Byte.class) {
			return (byte) cur.getShort(index);
		} else if (cls == boolean.class || cls == Boolean.class) {
			String string = cur.getString(index);
			return string == null ? false : Boolean.valueOf(string);
		} else if (cls == char.class || cls == Character.class) {
			String string = cur.getString(index);
			return string == null ? "" : string.charAt(0);
		} else {//不是基本类型是obj
			return null;
		}
	}

	@Override
	public String[] getValues(Field[] fields, Object obj) {
		String[] objs = new String[fields.length];
		try {
			for (int i = fields[0].getType() == String.class ? 0 : 1; i < objs.length; i++) {//如果主键是string，给主键赋值，如果主键是int，则自增
				objs[i] = String.valueOf(fields[i].get(obj));
			}
		} catch (Exception e) {
			LogUtils.e(e);
		}
		return objs;
	}

	@Override
	public String[] getValues(Field[] fields, Object obj, String[] query) {
		String[] objs = null;
		if (query == null) {
			objs = new String[fields.length];
		} else {
			objs = new String[fields.length + query.length];
			System.arraycopy(query, 0, objs, fields.length, query.length);
		}
		try {
			for (int i = 0; i < fields.length; i++) {
				objs[i] = String.valueOf(fields[i].get(obj));
			}
		} catch (Exception e) {
			LogUtils.e(e);
		}
		return objs;
	}
}
