package com.lib.sqlite.sqlbuilder;

import com.lib.sqlite.NameConvert;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by dsr on 2017/6/20.
 */

public class SqliteNameConvert implements NameConvert {
    private final HashMap<Class<?>, String> mNameCache = new HashMap<Class<?>, String>();

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
    public String getColumnName(Field bean) {
        return bean.getName();
    }


}
