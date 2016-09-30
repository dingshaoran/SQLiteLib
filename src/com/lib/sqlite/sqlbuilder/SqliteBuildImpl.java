package com.lib.sqlite.sqlbuilder;

import com.lib.sqlite.CacheSupport;
import com.lib.sqlite.SqlBuild;

import java.lang.reflect.Field;
import java.util.HashMap;

public class SqliteBuildImpl implements SqlBuild {
    private static final Character ADD = 'a';
    private static final Character UPDATE = 'u';
    private static final Character PUT = 'p';
    private static final Character QUERY = 'q';
    private static final Character DELETE = 'd';
    //缓存的量一般不超过1000条，内存占用小于1m不提供删除操作,如果要删除请使用LinkedHashMap
    private final HashMap<Class<?>, String> mNameCache = new HashMap<Class<?>, String>();
    private final HashMap<String, String> mSqlCache = new HashMap<String, String>();

    @Override
    public String getTableName(Class<?> bean) {
        String cache = mNameCache.get(bean);
        if (cache == null) {//把点转换为_,把原有的_转换为__
            cache = bean.getName().replace("_", "__").replace(".", "_");
            mNameCache.put(bean, cache);
        }
        return cache;
    }

    @Override
    public Class<?> getTableClass(String bean) {
        try {//getTableName的翻转方法
            return Class.forName(bean.replace("_", ".").replace("..", "_"));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Override
    public String createTable(CacheSupport clsCache, Class<?> cls) {
        Field[] fls = clsCache.getFieldWithClass(cls);
        StringBuilder builder = new StringBuilder(fls.length * 32 + 30);
        builder.append("CREATE TABLE ");
        builder.append(getTableName(cls));
        builder.append(" ( ");
        if (fls[0].getType() == String.class) {
            builder.append("\"").append(fls[0].getName()).append("\" TEXT PRIMARY KEY ,");
        } else {
            builder.append("\"").append(fls[0].getName()).append("\" INTEGER PRIMARY KEY ,");
        }
        for (int i = 1; i < fls.length; i++) {//i=0已经加到主键上了
            builder.append("\"").append(fls[i].getName()).append("\" TEXT ,");
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
    public String querySql(CacheSupport clsCache, Class<?> cls, String sel) {
        String cache = mSqlCache.get(getTableName(cls) + QUERY + sel);
        if (cache == null) {
            Field[] fls = clsCache.getFieldWithClass(cls);
            StringBuilder builder = new StringBuilder(fls.length * 40 + 30);
            builder.append("SELECT ");
            for (Field fl : fls) {
                builder.append(fl.getName()).append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(" FROM ");
            builder.append(getTableName(cls));
            if (sel != null) {
                builder.append(sel);
            }
            String s = builder.toString();
            mSqlCache.put(getTableName(cls) + QUERY + sel, s);
            return s;
        } else {
            return cache;
        }
    }

    @Override
    public String delete(Class<?> cls, String sel) {
        String cache = mSqlCache.get(getTableName(cls) + DELETE + sel);
        if (cache == null) {
            StringBuilder builder = new StringBuilder(100);
            builder.append("DELETE FROM ");
            builder.append(getTableName(cls));
            if (sel != null) {
                builder.append(sel);
            }
            String s = builder.toString();
            mSqlCache.put(getTableName(cls) + DELETE + sel, s);
            return s;
        } else {
            return cache;
        }
    }

    @Override
    public String updateSql(CacheSupport clsCache, Class<?> cls, String sel) {// INSERT OR REPLACE INTO
        String cache = mSqlCache.get(getTableName(cls) + UPDATE + sel);
        if (cache == null) {
            Field[] fls = clsCache.getFieldWithClass(cls);
            StringBuilder builder = new StringBuilder(fls.length * 40 + 30);
            builder.append("UPDATE ");
            builder.append(getTableName(cls));
            builder.append(" SET ");
            for (Field fl : fls) {//i=0已经加到主键上了
                builder.append(fl.getName()).append(" = ? ,");
            }
            builder.deleteCharAt(builder.length() - 1);
            if (sel != null) {
                builder.append(sel);
            }
            String s = builder.toString();
            mSqlCache.put(getTableName(cls) + UPDATE + sel, s);
            return s;
        } else {
            return cache;
        }
    }

    @Override
    public String addSql(CacheSupport clsCache, Class<?> cls) {
        String cache = mSqlCache.get(getTableName(cls) + ADD);
        if (cache == null) {
            Field[] fls = clsCache.getFieldWithClass(cls);
            StringBuilder builder = new StringBuilder(fls.length * 40 + 30);
            builder.append("INSERT INTO ");
            builder.append(getTableName(cls));
            builder.append(" ( ");
            for (Field fl1 : fls) {//i=0已经加到主键上了
                builder.append(fl1.getName()).append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(") VALUES (");
            for (Field ignored : fls) {
                builder.append("?,");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(")");
            String s = builder.toString();
            mSqlCache.put(getTableName(cls) + ADD, s);
            return s;
        } else {
            return cache;
        }
    }

    @Override
    public String updateColumn(Class<?> cls, String column) {
        return "ALTER TABLE " + getTableName(cls) + " ADD \"" + column + "\"  String";
    }

    @Override
    public String putSql(CacheSupport clsCache, Class<?> cls, String sel) {
        String cache = mSqlCache.get(getTableName(cls) + PUT + sel);
        if (cache == null) {
            Field[] fls = clsCache.getFieldWithClass(cls);
            StringBuilder builder = new StringBuilder(fls.length * 40 + 30);
            builder.append("INSERT OR REPLACE  INTO ");
            String simpleName = getTableName(cls);
            builder.append(simpleName);
            builder.append(" ( ");
            for (Field fl : fls) {//i=0已经加到主键上了
                builder.append(fl.getName()).append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(") VALUES (");
            if (sel != null) {
                builder.append("( SELECT ");
                builder.append(fls[0].getName());
                builder.append(" FROM ");
                builder.append(simpleName);
                builder.append(sel);
                builder.append(" )");
            } else {
                builder.append("?,");
            }
            for (int i = 1; i < fls.length; i++) {
                builder.append("?,");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(")");
            String s = builder.toString();
            mSqlCache.put(getTableName(cls) + PUT + sel, s);
            return s;
        } else {
            return cache;
        }
    }
}
