package com.lib.sqlite.sqlbuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import com.lib.sqlite.CacheSupport;
import com.lib.sqlite.anno.Id;
import com.lib.sqlite.anno.NotSave;

public class SqliteCacheISupport implements CacheSupport {
	//缓存的量一般不超过1000条，内存占用小于1m不提供删除操作,如果要删除请使用LinkedHashMap
	private final HashMap<Class<? extends Object>, Field[]> mFieldCache = new HashMap<Class<? extends Object>, Field[]>();

	@Override
	public <T> Field[] getFieldWithClass(Class<T> cls) {
		Field[] cache = mFieldCache.get(cls);
		if (cache == null) {
			Field[] fields = cls.getDeclaredFields();
			int length = fields.length;
			int i = 0;
			int swiLast = length - 1;
			while (i <= swiLast && fields[i] != null) {//如果有NotSave注解 就把当前的field和数组最后元素的交换。如果没有NotSave注解就判断下一个
				if (fields[i].getAnnotation(NotSave.class) != null || Modifier.isFinal(fields[i].getModifiers()) || Modifier.isStatic(fields[i].getModifiers())) {

					fields[i] = fields[swiLast];
					swiLast--;
				} else {
					fields[i].setAccessible(true);
					if (fields[i].getAnnotation(Id.class) != null) {//如果是带有id注解为主键，放到数组第一个
						Field temp = fields[i];
						fields[i] = fields[0];
						fields[0] = temp;
					}
					i++;
				}
			}
			if (swiLast + 1 != length) {//如果不等，代表swiLast--执行过，后面的元素都是NotSave的
				Field[] dst = new Field[swiLast + 1];
				System.arraycopy(fields, 0, dst, 0, swiLast + 1);
				mFieldCache.put(cls, dst);
				return dst;
			} else {
				mFieldCache.put(cls, fields);
				return fields;
			}
		} else {
			return cache;
		}
	}
}
