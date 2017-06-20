package com.lib.sqlite;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.lib.sqlite.sqlbuilder.SqliteBuildImpl;
import com.lib.sqlite.sqlbuilder.SqliteCacheISupport;
import com.lib.sqlite.sqlbuilder.SqliteCursorParser;
import com.lib.sqlite.sqlbuilder.SqliteNameConvert;
import com.lib.sqlite.sqlbuilder.SqliteDataConvert;
import com.lib.utils.LogUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class SqliteManager {
    public static final String TAG = "SqliteManager";
    private static final String DEFAULT = "Default";
    private static SqliteManager ins = new SqliteManager();
    private final Map<File, SqliteHandler> mDBs = new HashMap<File, SqliteHandler>();
    private final SqliteBuildImpl mSqliteBuild = new SqliteBuildImpl();
    private final SqliteCacheISupport mCacheISupport = new SqliteCacheISupport();
    private final SqliteCursorParser mCursorParser = new SqliteCursorParser();
    private final SqliteDataConvert mTypeConvert = new SqliteDataConvert();
    private final NameConvert mNameConvert = new SqliteNameConvert();

    private SqliteManager() {
    }

    public static SqliteManager getInstance() {
        return ins;
    }

    public SqliteHandler getHandle(Context context, File dbFile, NameConvert nameConvert, SqlBuild sqlBuild, CacheSupport cache, CursorParser curparser, DataConvert typeCvt) {
        SqliteHandler cur = mDBs.get(dbFile);
        if (cur == null) {
            synchronized (mDBs) {
                int newVersion = 0;
                try {
                    PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getApplicationInfo().packageName, PackageManager.GET_CONFIGURATIONS);
                    newVersion = pinfo.versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    LogUtils.e(SqliteManager.TAG, e);
                }
                cur = new SqliteHandler(dbFile, newVersion, nameConvert, sqlBuild, cache, curparser, typeCvt);
                mDBs.put(dbFile, cur);
            }
        } else {
            cur.setTypeCvt(typeCvt);
        }
        return cur;
    }

    public SqliteHandler getHandle(Context context, File file, SqliteDataConvert typeConvert) {
        return getHandle(context, file, mNameConvert, mSqliteBuild, mCacheISupport, mCursorParser, typeConvert);
    }


    public SqliteHandler getHandle(Context context, SqliteDataConvert.FormatObject fo) {
        return getHandle(context, context.getDatabasePath(DEFAULT), mNameConvert, mSqliteBuild, mCacheISupport, mCursorParser, new SqliteDataConvert(fo));
    }

    public SqliteHandler getHandle(Context context, SqliteDataConvert fo) {
        return getHandle(context, context.getDatabasePath(DEFAULT), mNameConvert, mSqliteBuild, mCacheISupport, mCursorParser, fo);
    }

    public SqliteHandler getHandle(Context context) {
        return getHandle(context, context.getDatabasePath(DEFAULT), mNameConvert, mSqliteBuild, mCacheISupport, mCursorParser, mTypeConvert);
    }

    public void release(File dbFile) {
        mDBs.remove(dbFile);
    }

    public void releaseAll() {
        mDBs.clear();
    }
}
