# SQLiteLib

简单的操作数据库的工具类

在application注入SqliteManager.init(context,list<Beans.class>);
SqliteManager.getHandle().   add/get/update/delete
优点 可以根据app清单文件的versionCode自动更新表结构。代码逻辑简单，分层清晰，有简单的缓存处理。
其中bean文件必须以@id标识主键，主键为int，long类型自增，string类型不自增。

如果有任何使用问题可以随时email ： dingshaoran@gmail.com
或者qq：850269838