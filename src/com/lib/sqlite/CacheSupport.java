package com.lib.sqlite;

import java.lang.reflect.Field;

public interface CacheSupport {
    /**
     * 缓存这个类中的所有存到数据库中的field，主键是第一个元素
     *
     * @param cls 字段所在的类
     * @return 要保存到数据库中的字段，第一个元素是主键
     */
    Field[] getFieldWithClass(Class<?> cls);
}
