package com.lib.sqlite.sqlbuilder;

import android.database.Cursor;

import com.lib.sqlite.DataConvert;
import com.lib.sqlite.SqliteManager;
import com.lib.utils.LogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class SqlliteTypeConvert implements DataConvert {
    private final FormatObject mFo;

    public SqlliteTypeConvert() {
        this(null);
    }

    public SqlliteTypeConvert(FormatObject fo) {
        if (fo == null) {
            mFo = new FormatObject() {
                @Override
                public Object fromString(Type cls, String si) {
                    if (cls == String.class) {
                        return si;
                    } else if (cls == int.class || cls == Integer.class) {
                        return Integer.valueOf(si);
                    } else if (cls == long.class || cls == Long.class) {
                        return Long.valueOf(si);
                    } else if (cls == double.class || cls == Double.class) {
                        return Double.valueOf(si);
                    } else if (cls == float.class || cls == Float.class) {
                        return Float.valueOf(si);
                    } else if (cls == short.class || cls == Short.class) {
                        return Short.valueOf(si);
                    } else if (cls == byte.class || cls == Byte.class) {
                        return Byte.valueOf(si);
                    } else if (cls == boolean.class || cls == Boolean.class) {
                        return si == null ? false : Boolean.valueOf(si);
                    } else if (cls == char.class || cls == Character.class) {
                        return si == null ? "" : si.charAt(0);
                    } else {//不是基本类型是obj
                        return null;
                    }
                }

                @Override
                public String toString(Object si) {
                    return String.valueOf(si);
                }
            };
        } else {
            this.mFo = fo;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getObjectValue(Cursor cur, Field[] fields, Class<T> cs) {
        T t = null;
        try {
            t = cs.newInstance();
        } catch (Exception e) {
            try {
                t = UnsafeAllocator.create().newInstance(cs);
            } catch (Exception e1) {
                LogUtils.e(SqliteManager.TAG, e1);
            }
        }
        for (int i = 0; i < fields.length; i++) {
            try {
                Class<?> cls = fields[i].getType();
                String si = cur.getString(i);
                fields[i].set(t, mFo.fromString(cls, si));
            } catch (Exception e) {
                LogUtils.e(SqliteManager.TAG, e);
            }
        }
        return t;
    }

    @Override
    public <T> Object getColumnValue(Cursor cur, Type cls, int index) {
        return mFo.fromString(cls, cur.getString(index));

    }

    @Override
    public String[] getValues(Field[] fields, Object obj) {
        String[] objs = new String[fields.length];
        try {
            for (int i = fields[0].getType() == String.class ? 0 : 1; i < objs.length; i++) {//如果主键是string，给主键赋值，如果主键是int，则自增
                objs[i] = mFo.toString(fields[i].get(obj));
            }
        } catch (Exception e) {
            LogUtils.e(SqliteManager.TAG, e);
        }
        return objs;
    }

    @Override
    public String[] getValues(Field[] fields, Object obj, String[] query) {
        String[] objs;
        if (query == null) {
            objs = new String[fields.length];
        } else {
            objs = new String[fields.length + query.length];
            System.arraycopy(query, 0, objs, fields.length, query.length);
        }
        try {
            for (int i = 0; i < fields.length; i++) {
                objs[i] = mFo.toString(fields[i].get(obj));
            }
        } catch (Exception e) {
            LogUtils.e(SqliteManager.TAG, e);
        }
        return objs;
    }

    @Override
    public String[] getPutValues(String[] query, int pos, Field[] fields, Object obj) {
        String[] objs;
        int length = 0;
        if (query == null) {
            objs = new String[fields.length];
        } else {
            length = query.length;
            objs = new String[fields.length + length - 1];
            System.arraycopy(query, 0, objs, pos, length);
        }
        try {
            for (int i = 0; i < fields.length; i++) {
                if (i < pos) {
                    objs[i] = mFo.toString(fields[i].get(obj));
                } else if (i > pos) {
                    objs[i + length - 1] = mFo.toString(fields[i].get(obj));
                }
            }
        } catch (Exception e) {
            LogUtils.e(SqliteManager.TAG, e);
        }
        return objs;
    }

    public interface FormatObject {

        Object fromString(Type type, String si);

        String toString(Object si);
    }
}
