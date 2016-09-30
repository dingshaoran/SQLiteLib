package com.lib.sqlite.sqlbuilder;

import java.lang.reflect.Field;

import com.lib.sqlite.CacheSupport;
import com.lib.sqlite.SqlBuild;
import com.lib.utils.DoubleKeyValueMap;

public class SqliteBuildImpl implements SqlBuild {
	//缓存的量一般不超过1000条，内存占用小于1m不提供删除操作,如果要删除请使用LinkedHashMap
	private final DoubleKeyValueMap<Class<? extends Object>, Object, String> mSqlCache = new DoubleKeyValueMap<Class<? extends Object>, Object, String>();
	private final Character CREATETABLE = 'c';
	private final Character ADD = 'a';
	private final Character UPDATE = 'u';
	private final Character PUT = 'p';

	@Override
	public String createTable(CacheSupport clsCache, Class<? extends Object> cls) {
		String cache = mSqlCache.get(cls, CREATETABLE);
		if (cache == null) {
			Field[] fls = clsCache.getFieldWithClass(cls);
			StringBuilder builder = new StringBuilder(fls.length * 32 + 30);
			builder.append("CREATE TABLE ");
			builder.append(cls.getSimpleName());
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
		} else {
			return cache;
		}
	}

	@Override
	public String getTableNames() {
		return "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name";
	}

	@Override
	public String querySql(CacheSupport clsCache, Class<? extends Object> cls, String sel) {
		String cache = mSqlCache.get(cls, sel);
		if (cache == null) {
			Field[] fls = clsCache.getFieldWithClass(cls);
			StringBuilder builder = new StringBuilder(fls.length * 40 + 30);
			builder.append("SELECT * FROM ");
			builder.append(cls.getSimpleName());
			if (sel != null) {
				builder.append(sel);
			}
			String s = builder.toString();
			mSqlCache.put(cls, sel, s);
			return s;
		} else {
			return cache;
		}
	}

	@Override
	public String delete(Class<? extends Object> cls, String sel) {
		String cache = mSqlCache.get(cls, sel);
		if (cache == null) {
			StringBuilder builder = new StringBuilder(100);
			builder.append("DELETE FROM ");
			builder.append(cls.getSimpleName());
			if (sel != null) {
				builder.append(sel);
			}
			String s = builder.toString();
			mSqlCache.put(cls, sel, s);
			return s;
		} else {
			return cache;
		}
	}

	@Override
	public String updateSql(CacheSupport clsCache, Class<? extends Object> cls, String sel) {// INSERT OR REPLACE INTO
		String cache = mSqlCache.get(cls, UPDATE);
		if (cache == null) {
			Field[] fls = clsCache.getFieldWithClass(cls);
			StringBuilder builder = new StringBuilder(fls.length * 40 + 30);
			builder.append("UPDATE ");
			builder.append(cls.getSimpleName());
			builder.append(" SET ");
			for (int i = 0; i < fls.length; i++) {//i=0已经加到主键上了
				builder.append(fls[i].getName()).append(" = ? ,");
			}
			builder.deleteCharAt(builder.length() - 1);
			if (sel != null) {
				builder.append(sel);
			}
			String s = builder.toString();
			mSqlCache.put(cls, UPDATE, s);
			return s;
		} else {
			return cache;
		}
	}

	@Override
	public String addSql(CacheSupport clsCache, Class<? extends Object> cls) {
		String cache = mSqlCache.get(cls, ADD);
		if (cache == null) {
			Field[] fls = clsCache.getFieldWithClass(cls);
			StringBuilder builder = new StringBuilder(fls.length * 40 + 30);
			builder.append("INSERT INTO ");
			builder.append(cls.getSimpleName());
			builder.append(" ( ");
			for (int i = 0; i < fls.length; i++) {//i=0已经加到主键上了
				builder.append(fls[i].getName()).append(",");
			}
			builder.deleteCharAt(builder.length() - 1);
			builder.append(") VALUES (");
			for (int i = 0; i < fls.length; i++) {
				builder.append("?,");
			}
			builder.deleteCharAt(builder.length() - 1);
			builder.append(")");
			String s = builder.toString();
			mSqlCache.put(cls, ADD, s);
			return s;
		} else {
			return cache;
		}
	}

	@Override
	public String updateColumn(Class<? extends Object> cls, String column) {
		return "ALTER TABLE " + cls.getSimpleName() + " ADD \"" + column + "\"  String";
	}

	@Override
	public String putSql(CacheSupport clsCache, Class<? extends Object> cls, String sel) {
		String cache = mSqlCache.get(cls, PUT);
		if (cache == null) {
			Field[] fls = clsCache.getFieldWithClass(cls);
			StringBuilder builder = new StringBuilder(fls.length * 40 + 30);
			builder.append("INSERT OR REPLACE  INTO ");
			builder.append(cls.getSimpleName());
			builder.append(" ( ");
			for (int i = 0; i < fls.length; i++) {//i=0已经加到主键上了
				builder.append(fls[i].getName()).append(",");
			}
			builder.deleteCharAt(builder.length() - 1);
			builder.append(") VALUES (");
			for (int i = 0; i < fls.length; i++) {
				builder.append("?,");
			}
			builder.deleteCharAt(builder.length() - 1);
			builder.append(")");
			if (sel != null) {
				builder.append(sel);
			}
			String s = builder.toString();
			mSqlCache.put(cls, PUT, s);
			return s;
		} else {
			return cache;
		}
	}
}
