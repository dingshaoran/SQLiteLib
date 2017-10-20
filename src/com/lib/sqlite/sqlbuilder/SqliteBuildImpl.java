package com.lib.sqlite.sqlbuilder;

import com.lib.sqlite.CacheSupport;
import com.lib.sqlite.NameConvert;
import com.lib.sqlite.SqlBuild;

import java.lang.reflect.Field;
import java.util.HashMap;

@SuppressWarnings("Convert2Diamond")
public class SqliteBuildImpl implements SqlBuild {
    private static final Character ADD = 'a';
    private static final Character UPDATE = 'u';
    private static final Character PUT = 'p';
    private static final Character QUERY = 'q';
    private static final Character DELETE = 'd';
    //缓存的量一般不超过1000条，内存占用小于1m不提供删除操作,如果要删除请使用LinkedHashMap
    private final HashMap<String, String> mSqlCache = new HashMap<String, String>();

    @Override
    public String createTable(CacheSupport clsCache, NameConvert vert, Class<?> cls) {
        Field[] fls = clsCache.getFieldWithClass(cls);
        StringBuilder builder = new StringBuilder(fls.length * 32 + 30);
        builder.append("CREATE TABLE ");
        builder.append(vert.getTableName(cls));
        builder.append(" ( ");
        if (fls[0].getType() == String.class) {
            builder.append("\"").append(vert.getColumnName(fls[0])).append("\" TEXT PRIMARY KEY ,");
        } else {
            builder.append("\"").append(vert.getColumnName(fls[0])).append("\" INTEGER PRIMARY KEY ,");
        }
        for (int i = 1; i < fls.length; i++) {//i=0已经加到主键上了
            builder.append("\"").append(vert.getColumnName(fls[i])).append("\" TEXT ,");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(" ) ");
        return builder.toString();
    }

    @Override
    public String getTableNames() {
        return "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name";
    }

    @Override
    public String querySql(String selectColumn, CacheSupport clsCache, NameConvert vert, Class<?> cls, String sel) {
        String tableName = vert.getTableName(cls);
        String cache = mSqlCache.get(tableName + selectColumn + QUERY + sel);
        if (cache == null) {
            Field[] fls = clsCache.getFieldWithClass(cls);
            StringBuilder builder = new StringBuilder(fls.length * 40 + 30);
            builder.append("SELECT ");
            if (selectColumn == null) {
                for (Field fl : fls) {
                    builder.append(vert.getColumnName(fl)).append(",");
                }
                builder.deleteCharAt(builder.length() - 1);
            } else {
                builder.append(selectColumn);
            }
            builder.append(" FROM ");
            builder.append(tableName);
            if (sel != null) {
                builder.append(sel);
            }
            String s = builder.toString();
            mSqlCache.put(tableName + selectColumn + QUERY + sel, s);
            return s;
        } else {
            return cache;
        }
    }

    @Override
    public String delete(Class<?> cls, NameConvert vert, String sel) {
        String tableName = vert.getTableName(cls);
        String cache = mSqlCache.get(tableName + DELETE + sel);
        if (cache == null) {
            StringBuilder builder = new StringBuilder(100);
            builder.append("DELETE FROM ");
            builder.append(tableName);
            if (sel != null) {
                builder.append(sel);
            }
            String s = builder.toString();
            mSqlCache.put(tableName + DELETE + sel, s);
            return s;
        } else {
            return cache;
        }
    }

    @Override
    public String updateSql(CacheSupport clsCache, NameConvert vert, Class<?> cls, String sel) {// INSERT OR REPLACE INTO
        String tableName = vert.getTableName(cls);
        String cache = mSqlCache.get(tableName + UPDATE + sel);
        if (cache == null) {
            Field[] fls = clsCache.getFieldWithClass(cls);
            StringBuilder builder = new StringBuilder(fls.length * 40 + 30);
            builder.append("UPDATE ");
            builder.append(tableName);
            builder.append(" SET ");
            for (Field fl : fls) {//i=0已经加到主键上了
                builder.append(vert.getColumnName(fl)).append(" = ? ,");
            }
            builder.deleteCharAt(builder.length() - 1);
            if (sel != null) {
                builder.append(sel);
            }
            String s = builder.toString();
            mSqlCache.put(tableName + UPDATE + sel, s);
            return s;
        } else {
            return cache;
        }
    }

    @Override
    public String addSql(CacheSupport clsCache, NameConvert vert, Class<?> cls) {
        String tableName = vert.getTableName(cls);
        String cache = mSqlCache.get(tableName + ADD);
        if (cache == null) {
            Field[] fls = clsCache.getFieldWithClass(cls);
            StringBuilder builder = new StringBuilder(fls.length * 40 + 30);
            builder.append("INSERT INTO ");
            builder.append(tableName);
            builder.append(" ( ");
            for (Field fl1 : fls) {//i=0已经加到主键上了
                builder.append(vert.getColumnName(fl1)).append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(") VALUES (");
            for (Field ignored : fls) {
                builder.append("?,");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(")");
            String s = builder.toString();
            mSqlCache.put(tableName + ADD, s);
            return s;
        } else {
            return cache;
        }
    }

    @Override
    public String updateColumn(Class<?> cls, NameConvert vert, String column) {
        return "ALTER TABLE " + vert.getTableName(cls) + " ADD \"" + column + "\"  String";
    }

    @Override
    public String putSql(CacheSupport clsCache, NameConvert vert, Class<?> cls, String sel) {
        String tableName = vert.getTableName(cls);
        String cache = mSqlCache.get(tableName + PUT + sel);
        if (cache == null) {
            Field[] fls = clsCache.getFieldWithClass(cls);
            StringBuilder builder = new StringBuilder(fls.length * 40 + 30);
            builder.append("INSERT OR REPLACE  INTO ");
            builder.append(tableName);
            builder.append(" ( ");
            for (Field fl : fls) {//i=0已经加到主键上了
                builder.append(vert.getColumnName(fl)).append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(") VALUES (");
            if (sel != null) {//查找 id
                builder.append("( SELECT ");
                builder.append(vert.getColumnName(fls[0]));
                builder.append(" FROM ");
                builder.append(tableName);
                builder.append(sel);
                builder.append(" ),");
            } else {
                builder.append("?,");
            }
            for (int i = 1; i < fls.length; i++) {
                builder.append("?,");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(")");
            String s = builder.toString();
            mSqlCache.put(tableName + PUT + sel, s);
            return s;
        } else {
            return cache;
        }
    }
}
