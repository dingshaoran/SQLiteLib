package com.lib.sqlite;

import java.lang.reflect.Field;

public interface CacheSupport {
	/**
	 * 缓存这个类中的所有存到数据库中的field，主键是第一个元素
	 * @param cls
	 * @return
	 */
	<T> Field[] getFieldWithClass(Class<T> cls);
}
