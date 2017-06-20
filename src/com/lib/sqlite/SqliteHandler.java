package com.lib.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lib.sqlite.sqlbuilder.QueryData;
import com.lib.sqlite.sqlbuilder.QueryData.QueryBuilder;
import com.lib.sqlite.sqlbuilder.QueryData.QueryOpera;
import com.lib.utils.LogUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess", "Convert2Diamond"})
public final class SqliteHandler {
    private final HashSet<Class<?>> mTables = new HashSet<Class<?>>(); //已经创建了 对应表的 class
    private final NameConvert mNameConvert;
    private SQLiteDatabase mDB;
    private SqlBuild mSqlBuild;
    private CacheSupport mCache;
    private CursorParser mCurParser;
    private DataConvert mTypeCvt;

    public SqliteHandler(File dbFile, int version, NameConvert nameConvert, SqlBuild sqlBuild, CacheSupport cache, CursorParser curparser, DataConvert typeCvt) {
        mSqlBuild = sqlBuild;
        mCache = cache;
        mCurParser = curparser;
        this.mTypeCvt = typeCvt;
        this.mNameConvert = nameConvert;
        //noinspection ResultOfMethodCallIgnored
        dbFile.getParentFile().mkdirs();
        mDB = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        try {
            ArrayList<String> names = getTableNames();
            for (String name : names) {
                Class<?> cls = mNameConvert.getTableClass(name);
                if (cls != null) {
                    mTables.add(cls);
                }
            }
            int oldVersion = mDB.getVersion();
            if (oldVersion != version) {
                try {
                    mDB.beginTransaction();
                    updateTable();
                    mDB.setVersion(version);
                    mDB.setTransactionSuccessful();
                } catch (Exception e) {
                    LogUtils.e(SqliteManager.TAG, e);
                } finally {
                    mDB.endTransaction();
                }
            }
        } catch (Exception e) {
            LogUtils.e(SqliteManager.TAG, e);
        }
    }

    protected void setTypeCvt(DataConvert mTypeCvt) {
        this.mTypeCvt = mTypeCvt;
    }

    private void createTable(Class<?> bean) {
        synchronized (mTables) {
            if (!mTables.contains(bean)) {
                String table = mSqlBuild.createTable(mCache, mNameConvert, bean);
                LogUtils.i(SqliteManager.TAG, table);
                mDB.execSQL(table);
                mTables.add(bean);
            }
        }
    }

    private void updateTable() {
        for (Class<?> key : mTables) {
            Cursor cursor = getCursor("*", key, new QueryBuilder().limit(0, 1).build());
            for (Field field : mCache.getFieldWithClass(key)) {
                String name = mNameConvert.getColumnName(field);
                if (cursor.getColumnIndex(name) == -1) {
                    String updateColumn = mSqlBuild.updateColumn(key, mNameConvert, name);
                    LogUtils.i(SqliteManager.TAG, updateColumn);
                    mDB.execSQL(updateColumn);
                }
            }
        }
    }

    private ArrayList<String> getTableNames() {
        String tableNames = mSqlBuild.getTableNames();
        LogUtils.i(SqliteManager.TAG, tableNames);
        Cursor c = mDB.rawQuery(tableNames, null);
        return mCurParser.parseColumn(mTypeCvt, c, String.class, null);
    }

    /**
     * 插入一条数据    主键必须带注解 @id ，如果主键是String不自增，如果主键不是string自增
     * 不保存的字段使用 @NotSave 注解表明
     *
     * @param obj 要添加的数据
     */
    public boolean add(Object obj) {
        try {
            Class<?> cls = obj.getClass();
            createTable(cls);
            Field[] fields = mCache.getFieldWithClass(cls);
            String[] values = mTypeCvt.getValues(fields, obj);
            if (fields[0].getType() != String.class) {
                values[0] = null;
            }
            String addSql = mSqlBuild.addSql(mCache, mNameConvert, cls);
            LogUtils.i(SqliteManager.TAG, addSql);
            mDB.execSQL(addSql, values);
            return true;
        } catch (Exception e) {
            LogUtils.e(SqliteManager.TAG, e);
            return false;
        }
    }

    /**
     * 插入多条数据（有序）list中的object必须为同一种类型的bean，<br/>
     * 主键必须带注解 @id ，如果主键是String不自增，如果主键不是string自增，不保存的字段使用 @NotSave 注解表明
     *
     * @param list 要添加的数据
     */
    public boolean addAll(List<?> list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        Object obj_0 = list.get(0);
        if (obj_0 instanceof Class) {
            throw new RuntimeException("第一个参数不能为class");
        }
        Class<?> cls_0 = obj_0.getClass();
        Field[] fields = mCache.getFieldWithClass(cls_0);
        boolean typeString = fields[0].getType() != String.class;
        mDB.beginTransaction();
        try {
            createTable(obj_0.getClass());
            String addSql = mSqlBuild.addSql(mCache, mNameConvert, cls_0);
            LogUtils.i(SqliteManager.TAG, addSql);
            for (int i = 0; i < list.size(); i++) {
                Object obj = list.get(i);
                String[] values = mTypeCvt.getValues(fields, obj);
                if (typeString) {
                    values[0] = null;
                }
                mDB.execSQL(addSql, values);
            }
            mDB.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            LogUtils.e(SqliteManager.TAG, e);
            return false;
        } finally {
            mDB.endTransaction();
        }
    }

    /**
     * 更新一条数据，可以传入的参数为object，null。或者class，QueryData。
     *
     * @param obj 要更新的数据
     * @param sel 查找条件
     * @return 更新成功返回 true
     */
    public boolean update(Object obj, QueryData sel) {
        try {
            createTable(obj.getClass());
            Class<?> cls = null;
            Field[] fields = null;
            if (obj instanceof Class) {
                if (sel == null) {
                    throw new RuntimeException("第一个参数为class，第二个参数不能为null");
                } else {
                    fields = mCache.getFieldWithClass((Class<?>) obj);
                }
            } else {
                cls = obj.getClass();
                if (sel == null) {
                    fields = mCache.getFieldWithClass(cls);
                    String name = mNameConvert.getColumnName(fields[0]);
                    sel = new QueryBuilder().where(name, QueryOpera.equal, String.valueOf(fields[0].get(obj))).build();
                }
            }
            String[] values = mTypeCvt.getValues(fields, obj, sel.getParams());
            String updateSql = mSqlBuild.updateSql(mCache, mNameConvert, cls, sel.getQuery());
            LogUtils.i(SqliteManager.TAG, updateSql);
            mDB.execSQL(updateSql, values);
            return true;
        } catch (Exception e) {
            LogUtils.e(SqliteManager.TAG, e);
            return false;
        }
    }

    /**
     * 更新所有数据，list中的object必须为同一种类型的bean
     *
     * @param list 要更新的所有数据
     * @param sel  更新的条件
     * @return 更新成功返回 true
     */
    public boolean updateAll(List<?> list, QueryData sel) {
        if (list == null || list.size() == 0) {
            return true;
        }
        Object obj_0 = list.get(0);
        if (obj_0 instanceof Class) {
            throw new RuntimeException("第一个参数不能为class");
        }
        Class<?> cls_0 = obj_0.getClass();
        Field[] fields = mCache.getFieldWithClass(cls_0);
        String idName = mNameConvert.getColumnName(fields[0]);
        mDB.beginTransaction();
        try {
            createTable(obj_0.getClass());
            if (sel == null) {
                sel = new QueryBuilder().where(idName, QueryOpera.equal, "").build();
                String sql = mSqlBuild.updateSql(mCache, mNameConvert, cls_0, sel.getQuery());
                LogUtils.i(SqliteManager.TAG, sql);
                for (int i = 0; i < list.size(); i++) {
                    Object obj = list.get(i);
                    String[] values = mTypeCvt.getValues(fields, obj, new String[]{String.valueOf(fields[0].get(obj))});
                    mDB.execSQL(sql, values);
                }
            } else {
                for (int i = 0; i < list.size(); i++) {
                    Object obj = list.get(i);
                    Object[] values = mTypeCvt.getValues(fields, obj, sel.getParams());
                    String s = mSqlBuild.updateSql(mCache, mNameConvert, cls_0, sel.getQuery());
                    LogUtils.i(SqliteManager.TAG, s);
                    mDB.execSQL(s, values);
                }
            }
            mDB.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            LogUtils.e(SqliteManager.TAG, e);
            return false;
        } finally {
            mDB.endTransaction();
        }
    }

    /**
     * 查找符合条件的所有数据
     *
     * @param cls 要查找的类
     * @param sel 查找条件
     * @return 数据库中的对应数据
     */
    public <T> ArrayList<T> get(Class<T> cls, QueryData sel) {
        try {
            return mCurParser.parseObject(mTypeCvt, getCursor(null, cls, sel), mCache, cls);
        } catch (Exception e) {
            LogUtils.e(SqliteManager.TAG, e);
            return new ArrayList<T>();
        }
    }

    /**
     * 查找一个字段的数据
     *
     * @param cls 要查找的类 Class<T> 必须是8种基本类型或者 String
     * @param sel 查找条件
     * @return 数据库中的对应数据
     */
    public <T> ArrayList<T> getColumn(Class cls, String columnName, QueryData sel) {
        try {
            Class<?> type;
            try {
                type = cls.getField(columnName).getType();
            } catch (NoSuchFieldException e) {
                type = String.class;
            }
            return mCurParser.parseColumn(mTypeCvt, getCursor(columnName, cls, sel), type, columnName);
        } catch (Exception e) {
            LogUtils.e(SqliteManager.TAG, e);
            return new ArrayList<T>();
        }
    }

    /**
     * 同 {@link #get(Class, QueryData)}  就是返回cursor要自己解析
     *
     * @param cls 要查找的类 class
     * @param sel 查找条件
     * @return 数据库中的对应数据
     */
    public <T> Cursor getCursor(String selectColumn, Class<T> cls, QueryData sel) {
        if (sel == null) {
            String query = mSqlBuild.querySql(selectColumn, mCache, mNameConvert, cls, null);
            LogUtils.i(SqliteManager.TAG, query);
            return mDB.rawQuery(query, null);
        } else {
            String query = mSqlBuild.querySql(selectColumn, mCache, mNameConvert, cls, sel.getQuery());
            LogUtils.i(SqliteManager.TAG, query);
            return mDB.rawQuery(query, sel.getParams());
        }
    }

    /**
     * 插入或者修改数据
     *
     * @param obj 要操作的数据
     * @param sel 查找条件
     * @return 是否更新成功
     */
    public boolean put(Object obj, QueryData sel) {
        try {
            createTable(obj.getClass());
            Class<?> cls = obj.getClass();
            Field[] fields = mCache.getFieldWithClass(cls);
            if (sel == null) {
                String[] values = mTypeCvt.getValues(fields, obj);
                String s = mSqlBuild.putSql(mCache, mNameConvert, cls, null);
                LogUtils.i(SqliteManager.TAG, s);
                mDB.execSQL(s, values);
            } else {
                String[] values = mTypeCvt.getPutValues(sel.getParams(), 0, fields, obj);
                String s = mSqlBuild.putSql(mCache, mNameConvert, cls, sel.getQuery());
                LogUtils.i(SqliteManager.TAG, s);
                mDB.execSQL(s, values);
            }
            return true;
        } catch (Exception e) {
            LogUtils.e(SqliteManager.TAG, e);
            return false;
        }
    }

    /**
     * 要插入或者更新所有数据，list中的object必须为同一种类型的bean
     *
     * @param list 要插入的数据集合
     * @param sel  要更新 sql
     * @return 是否更新成功
     */
    public boolean putAll(List<?> list, List<QueryData> sel) {
        if (list == null || list.size() == 0) {
            return true;
        }
        Object obj_0 = list.get(0);
        if (obj_0 instanceof Class) {
            throw new RuntimeException("第一个参数不能为class");
        }
        Class<?> cls_0 = obj_0.getClass();
        Field[] fields = mCache.getFieldWithClass(cls_0);
        String idnName = mNameConvert.getColumnName(fields[0]);
        mDB.beginTransaction();
        try {
            createTable(obj_0.getClass());
            if (sel == null) {
                String putSql = mSqlBuild.putSql(mCache, mNameConvert, cls_0, null);
                LogUtils.i(SqliteManager.TAG, putSql);
                for (int i = 0; i < list.size(); i++) {
                    Object obj = list.get(i);
                    String[] values = mTypeCvt.getValues(fields, obj);
                    mDB.execSQL(putSql, values);
                }
            } else {
                for (int i = 0; i < list.size(); i++) {
                    Object obj = list.get(i);
                    QueryData queryData = sel.get(i);
                    if (queryData == null) {
                        String[] values = mTypeCvt.getValues(fields, obj);
                        String s = mSqlBuild.putSql(mCache, mNameConvert, cls_0, null);
                        LogUtils.i(SqliteManager.TAG, s);
                        mDB.execSQL(s, values);
                    } else {
                        String[] values = mTypeCvt.getPutValues(queryData.getParams(), 0, fields, obj);
                        String putSql = mSqlBuild.putSql(mCache, mNameConvert, cls_0, queryData.getQuery());
                        LogUtils.i(SqliteManager.TAG, putSql);
                        mDB.execSQL(putSql, values);
                    }
                }
            }
            mDB.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            LogUtils.e(SqliteManager.TAG, e);
            return false;
        } finally {
            mDB.endTransaction();
        }
    }

    public boolean delete(Object obj, QueryData sel) {
        try {
            Class<?> clz;
            if (obj instanceof Class) {
                clz = (Class<?>) obj;
                if (sel == null) {
                    throw new RuntimeException("如果obj是class文件，sel不能为空");
                }
            } else {
                clz = obj.getClass();
                if (sel == null) {
                    Field field = mCache.getFieldWithClass(clz)[0];
                    sel = new QueryBuilder().where(mNameConvert.getColumnName(field), QueryOpera.equal, String.valueOf(field.get(obj))).build();
                }
            }

            String delete = mSqlBuild.delete(clz, mNameConvert, sel.getQuery());
            LogUtils.i(SqliteManager.TAG, delete);
            mDB.execSQL(delete, sel.getParams());
            return true;
        } catch (Exception e) {
            LogUtils.e(SqliteManager.TAG, e);
            return false;
        }
    }

    public boolean deleteAll(List<?> list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        Object obj_0 = list.get(0);
        if (obj_0 instanceof Class) {
            throw new RuntimeException("第一个参数不能为class");
        }
        Class<?> cls_0 = obj_0.getClass();
        Field field = mCache.getFieldWithClass(cls_0)[0];
        String idName = mNameConvert.getColumnName(field);
        mDB.beginTransaction();
        try {
            QueryData build = new QueryBuilder().where(idName, QueryOpera.equal, "").build();
            String sql = mSqlBuild.delete(cls_0, mNameConvert, build.getQuery());
            LogUtils.i(SqliteManager.TAG, sql);
            for (int i = 0; i < list.size(); i++) {
                Object obj = list.get(i);
                mDB.execSQL(sql, new String[]{String.valueOf(field.get(obj))});
            }
            mDB.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            LogUtils.e(SqliteManager.TAG, e);
            return false;
        } finally {
            mDB.endTransaction();
        }
    }
}
