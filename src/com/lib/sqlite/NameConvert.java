package com.lib.sqlite;

import java.lang.reflect.Field;

public interface NameConvert {

    /**
     * 通过cls 获取 tab 名称
     */
    String getTableName(Class<?> bean);

    /**
     * 通过 tab 名称获取类
     */
    Class<?> getTableClass(String bean);

    String getColumnName(Field bean);
}
