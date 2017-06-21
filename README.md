# SQLiteLib

简单的操作数据库的工具类

优点：
1，可以根据apk的versionCode自动升级表结构（老版本的数据取出来是空值）；
2，可以传进去一个DataConvert(把Object类型数据转换为string  gson就可以)保存Object类型数据。
3，速度快，占用内存小。

基本使用方法：
其中bean文件必须含有字段名为id或者@id标识  当做主键，主键为int，long类型自增，string类型不自增，@NotSave标示不用保存的字段。

SqliteManager.getInstance().getHandle(context).add/get/update/put/delete

gethandle(context,file) 传入一个file，可以指定数据库的保存位置
gethandle(context,file,FormatObject) 传入一个接口实现类实现toString和fromString方法 按照自己想要的类型转换数据，从数据库中读出来的是string，经过接口的实现方法转换为想要的数据类型，可以用gson实现，把object类型字段转换为一个string保存

getHandle还重载了更多参数的方法，可以自己转换保存的字段名，sql拼写，缓存处理等等。

handle 是带有缓存的通过更改SqliteManager中的CacheSize的大小控制缓存的量。

