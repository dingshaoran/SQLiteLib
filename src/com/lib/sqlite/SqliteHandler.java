package com.lib.sqlite;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lib.sqlite.sqlbuilder.QueryData;
import com.lib.sqlite.sqlbuilder.QueryData.QueryBuilder;
import com.lib.sqlite.sqlbuilder.QueryData.QueryOpera;
import com.lib.sqlite.sqlbuilder.SqliteBuildImpl;
import com.lib.sqlite.sqlbuilder.SqliteCacheISupport;
import com.lib.sqlite.sqlbuilder.SqliteCursorParser;
import com.lib.sqlite.sqlbuilder.SqlliteTypeConvert;
import com.lib.utils.LogUtils;

public final class SqliteHandler {

	private SQLiteDatabase mDB;
	private SqlBuild mSqlBuild;
	private CacheSupport mCache;
	private CursorParser mCurParser;
	private TypeConVert mTypeCvt;
	private List<Class<? extends Object>> beans;

	private SqliteHandler() {
	}

	private void init(Context context, List<Class<? extends Object>> beans, File file) {
		this.beans = beans;
		file.getParentFile().mkdirs();
		mDB = SQLiteDatabase.openOrCreateDatabase(file, null);
		try {
			ArrayList<String> names = getTableNames();
			for (Class<? extends Object> bean : beans) {
				if (!names.contains(bean.getSimpleName())) {
					mDB.execSQL(mSqlBuild.createTable(mCache, bean));
				}
			}
			int oldVersion = mDB.getVersion();
			PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getApplicationInfo().packageName, PackageManager.GET_CONFIGURATIONS);
			int newVersion = pinfo.versionCode;
			if (oldVersion != newVersion) {
				try {
					mDB.beginTransaction();
					for (Class<? extends Object> c : beans) {
						Cursor cursor = getCursor(c, new QueryBuilder().limit(0, 1).build());
						for (Field field : mCache.getFieldWithClass(c)) {
							if (cursor.getColumnIndex(field.getName()) == -1) {
								mDB.execSQL(mSqlBuild.updateColumn(c, field.getName()));
							}
						}
					}
					mDB.setVersion(newVersion);
					mDB.setTransactionSuccessful();
				} catch (Exception e) {
					LogUtils.e(e);
				} finally {
					mDB.endTransaction();
				}
			}
		} catch (Exception e) {
			LogUtils.e(e);
		}
	}

	private ArrayList<String> getTableNames() {
		Cursor c = mDB.rawQuery(mSqlBuild.getTableNames(), null);
		return mCurParser.parseCursor(mTypeCvt, beans, c, mCache, String.class);
	}

	/**
	 * 插入一条数据    主键必须带注解 @id ，如果主键是String不自增，如果主键不是string自增
	 * 不保存的字段使用 @NotSave 注解表明
	 * @param obj 要添加的数据
	 */
	public void add(Object obj) {
		Class<? extends Object> cls = obj.getClass();
		Field[] fields = mCache.getFieldWithClass(cls);
		String[] values = mTypeCvt.getValues(fields, obj);
		if (fields[0].getType() != String.class) {
			values[0] = null;
		}
		mDB.execSQL(mSqlBuild.addSql(mCache, cls), values);
	}

	/**
	 * 插入多条数据（有序）list中的object必须为同一种类型的bean，<br/>
	 *  主键必须带注解 @id ，如果主键是String不自增，如果主键不是string自增，不保存的字段使用 @NotSave 注解表明
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
		Class<? extends Object> cls_0 = obj_0.getClass();
		Field[] fields = mCache.getFieldWithClass(cls_0);
		boolean typeString = fields[0].getType() != String.class;
		mDB.beginTransaction();
		try {
			String addSql = mSqlBuild.addSql(mCache, cls_0);
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
			return false;
		} finally {
			mDB.endTransaction();
		}
	}

	/**
	 * 更新一条数据，可以传入的参数为object，null。或者class，QueryData。
	 * @param obj 要更新的数据
	 * @param sel 查找条件
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean update(Object obj, QueryData sel) {
		try {
			Class<? extends Object> cls = null;
			Field[] fields = null;
			if (obj instanceof Class) {
				cls = (Class<? extends Object>) obj;
				if (sel == null) {
					throw new RuntimeException("第一个参数为class，第二个参数不能为null");
				}
				fields = mCache.getFieldWithClass(cls);
			} else {
				cls = obj.getClass();
				if (sel == null) {
					fields = mCache.getFieldWithClass(cls);
					String name = fields[0].getName();
					sel = new QueryBuilder().where(name, QueryOpera.equal, String.valueOf(fields[0].get(obj))).build();
				}
			}
			String[] values = mTypeCvt.getValues(fields, obj, sel.getParams());
			mDB.execSQL(mSqlBuild.updateSql(mCache, cls, sel.getQuery()), values);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 更新所有数据，list中的object必须为同一种类型的bean
	 * @param list 要更新的所有数据
	 * @param sel 更新的条件
	 * @return
	 */
	public boolean updateAll(List<?> list, QueryData sel) {
		if (list == null || list.size() == 0) {
			return true;
		}
		Object obj_0 = list.get(0);
		if (obj_0 instanceof Class) {
			throw new RuntimeException("第一个参数不能为class");
		}
		Class<? extends Object> cls_0 = obj_0.getClass();
		Field[] fields = mCache.getFieldWithClass(cls_0);
		String idName = fields[0].getName();
		mDB.beginTransaction();
		try {
			if (sel == null) {
				sel = new QueryBuilder().where(idName, QueryOpera.equal, "").build();
				String sql = mSqlBuild.updateSql(mCache, cls_0, sel.getQuery());
				for (int i = 0; i < list.size(); i++) {
					Object obj = list.get(i);
					String[] values = mTypeCvt.getValues(fields, obj, new String[] { String.valueOf(fields[0].get(obj)) });
					mDB.execSQL(sql, values);
				}
			} else {
				for (int i = 0; i < list.size(); i++) {
					Object obj = list.get(i);
					Object[] values = mTypeCvt.getValues(fields, obj, sel.getParams());
					mDB.execSQL(mSqlBuild.updateSql(mCache, cls_0, sel.getQuery()), values);
				}
			}
			mDB.setTransactionSuccessful();
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			mDB.endTransaction();
		}
	}

	/**
	 * 查找符合条件的所有数据
	 * @param cls 要查找的类
	 * @param sel 查找条件
	 * @return
	 */
	public <T> ArrayList<T> get(Class<T> cls, QueryData sel) {
		return mCurParser.parseCursor(mTypeCvt, beans, getCursor(cls, sel), mCache, cls);
	}

	/**
	 * 同 {@link #get(Class, QueryData)}  就是返回cursor要自己解析
	 * @param cls
	 * @param sel
	 * @return
	 */
	public <T> Cursor getCursor(Class<T> cls, QueryData sel) {
		if (sel == null) {
			return mDB.rawQuery(mSqlBuild.querySql(mCache, cls, null), null);
		} else {
			return mDB.rawQuery(mSqlBuild.querySql(mCache, cls, sel.getQuery()), sel.getParams());
		}
	}

	/**
	 * 插入或者更新数据
	 * @param obj 要操作的数据
	 * @param sel 查找条件
	 * @return
	 */
	public boolean put(Object obj, QueryData sel) {
		try {
			Class<? extends Object> cls = obj.getClass();
			Field[] fields = mCache.getFieldWithClass(cls);
			if (sel == null) {
				String name = fields[0].getName();
				sel = new QueryBuilder().where(name, QueryOpera.equal, String.valueOf(fields[0].get(obj))).build();
			}
			String[] values = mTypeCvt.getValues(fields, obj, sel.getParams());
			mDB.execSQL(mSqlBuild.putSql(mCache, cls, sel.getQuery()), values);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 要插入或者更新所有数据，list中的object必须为同一种类型的bean
	 * @param list
	 * @param sel
	 * @return
	 */
	public boolean putAll(List<?> list, QueryData sel) {
		if (list == null || list.size() == 0) {
			return true;
		}
		Object obj_0 = list.get(0);
		if (obj_0 instanceof Class) {
			throw new RuntimeException("第一个参数不能为class");
		}
		Class<? extends Object> cls_0 = obj_0.getClass();
		Field[] fields = mCache.getFieldWithClass(cls_0);
		String idnName = fields[0].getName();
		mDB.beginTransaction();
		try {
			if (sel == null) {
				sel = new QueryBuilder().where(idnName, QueryOpera.equal, "").build();
				String putSql = mSqlBuild.putSql(mCache, cls_0, sel.getQuery());
				for (int i = 0; i < list.size(); i++) {
					Object obj = list.get(i);
					String[] values = mTypeCvt.getValues(fields, obj, new String[] { String.valueOf(fields[0].get(obj)) });
					mDB.execSQL(putSql, values);
				}
			} else {
				for (int i = 0; i < list.size(); i++) {
					Object obj = list.get(i);
					String[] values = mTypeCvt.getValues(fields, obj, sel.getParams());
					mDB.execSQL(mSqlBuild.putSql(mCache, cls_0, sel.getQuery()), values);
				}
			}
			mDB.setTransactionSuccessful();
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			mDB.endTransaction();
		}
	}

	@SuppressWarnings("unchecked")
	public boolean delete(Object obj, QueryData sel) {
		try {
			Class<? extends Object> clz = null;
			if (obj instanceof Class) {
				clz = (Class<? extends Object>) obj;
				if (sel == null) {
					throw new RuntimeException("如果obj是class文件，sel不能为空");
				}
			} else {
				clz = obj.getClass();
				if (sel == null) {
					Field field = mCache.getFieldWithClass(clz)[0];
					sel = new QueryBuilder().where(field.getName(), QueryOpera.equal, String.valueOf(field.get(obj))).build();
				}
			}
			mDB.execSQL(mSqlBuild.delete(clz, sel.getQuery()), sel.getParams());
			return true;
		} catch (Exception e) {
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
		Class<? extends Object> cls_0 = obj_0.getClass();
		Field field = mCache.getFieldWithClass(cls_0)[0];
		String idName = field.getName();
		mDB.beginTransaction();
		try {
			QueryData build = new QueryBuilder().where(idName, QueryOpera.equal, "").build();
			String sql = mSqlBuild.delete(cls_0, build.getQuery());
			for (int i = 0; i < list.size(); i++) {
				Object obj = list.get(i);
				mDB.execSQL(sql, new String[] { String.valueOf(field.get(obj)) });
			}
			mDB.setTransactionSuccessful();
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			mDB.endTransaction();
		}
	}

	public final static class Builder {
		private final Context context;
		private final File file;
		private final List<Class<? extends Object>> beans;
		private SqlBuild build;
		private CacheSupport cache;
		private CursorParser curParser;
		private TypeConVert tyc;

		protected Builder(Context context, List<Class<? extends Object>> objs, File file) {
			this.context = context;
			this.beans = objs;
			this.file = file;
		}

		public void setSqlBuild(SqlBuild build) {
			this.build = build;
		}

		public void setClassCache(CacheSupport cache) {
			this.cache = cache;
		}

		public void setCurParser(CursorParser curParser) {
			this.curParser = curParser;
		}

		public SqliteHandler build() {
			SqliteHandler handler = new SqliteHandler();
			if (build == null) {
				handler.mSqlBuild = new SqliteBuildImpl();
			} else {
				handler.mSqlBuild = build;
			}
			if (cache == null) {
				handler.mCache = new SqliteCacheISupport();
			} else {
				handler.mCache = cache;
			}
			if (curParser == null) {
				handler.mCurParser = new SqliteCursorParser();
			} else {
				handler.mCurParser = curParser;
			}
			if (tyc == null) {
				handler.mTypeCvt = new SqlliteTypeConvert();
			} else {
				handler.mTypeCvt = tyc;
			}
			handler.init(context, beans, file);
			return handler;
		}
	}
}
