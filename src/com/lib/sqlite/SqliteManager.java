package com.lib.sqlite;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.lib.sqlite.sqlbuilder.SqliteBuildImpl;
import com.lib.sqlite.sqlbuilder.SqliteCacheISupport;
import com.lib.sqlite.sqlbuilder.SqliteCursorParser;
import com.lib.sqlite.sqlbuilder.SqliteDataConvert;
import com.lib.sqlite.sqlbuilder.SqliteNameConvert;
import com.lib.utils.LogUtils;

import java.io.File;
import java.util.LinkedHashMap;

@SuppressWarnings({"unused", "WeakerAccess", "Convert2Diamond"})
public class SqliteManager {
    public static final String TAG = "SqliteManager";
    public static int CACHESIZE = 5;
    private static final String DEFAULT = "Default";
    private static SqliteManager ins = new SqliteManager();
    private final LinkedHashMap<String, SqliteHandler> mDBs = new LinkedHashMap<String, SqliteHandler>(10, 0.75f, true);
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
        String key = dbFile.toString() + typeCvt.toString();
        SqliteHandler cur = mDBs.get(key);
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
                mDBs.put(key, cur);
                int size = mDBs.size();
                for (int i = 0; i < size - CACHESIZE; i++) {//已经大于缓存个数的删掉
                    mDBs.remove(mDBs.keySet().iterator().next());
                }
            }
        } else {
            cur.setTypeCvt(typeCvt);
        }
        return cur;
    }

    public SqliteHandler getHandle(Context context, File file, DataConvert typeConvert) {
        return getHandle(context, file, mNameConvert, mSqliteBuild, mCacheISupport, mCursorParser, typeConvert);
    }

    public SqliteHandler getHandle(Context context, File file) {
        return getHandle(context, file, mNameConvert, mSqliteBuild, mCacheISupport, mCursorParser, mTypeConvert);
    }


    public SqliteHandler getHandle(Context context, SqliteDataConvert.FormatObject fo) {
        return getHandle(context, context.getDatabasePath(DEFAULT), mNameConvert, mSqliteBuild, mCacheISupport, mCursorParser, new SqliteDataConvert(fo));
    }

    public SqliteHandler getHandle(Context context, DataConvert fo) {
        return getHandle(context, context.getDatabasePath(DEFAULT), mNameConvert, mSqliteBuild, mCacheISupport, mCursorParser, fo);
    }

    public SqliteHandler getHandle(Context context) {
        return getHandle(context, context.getDatabasePath(DEFAULT), mNameConvert, mSqliteBuild, mCacheISupport, mCursorParser, mTypeConvert);
    }

    public void release(File dbFile, DataConvert typeConvert) {
        mDBs.remove(dbFile.toString() + typeConvert.toString());
    }

    public void release(File dbFile) {
        mDBs.remove(dbFile.toString() + mTypeConvert.toString());
    }

    public void releaseAll() {
        mDBs.clear();
    }
}
