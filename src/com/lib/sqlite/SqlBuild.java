package com.lib.sqlite;

public interface SqlBuild {

    /**
     * 获取数据库所有的表名
     *
     * @return sql
     */
    String getTableNames();

    /**
     * 更新数据库中的一列（sqlite中每次只能更新一列）
     *
     * @param cls    通过clas获取表名
     * @param column 要更新的列名
     * @return sql 语句
     */
    String updateColumn(Class<?> cls, NameConvert vert, String column);

    /**
     * 查询表中数据
     *
     * @param clsCache 通过cls获取查询字段
     * @param cls      要查询的表
     * @param sel      要查询的条件
     * @return sql 语句
     */
    String querySql(String selectColumn,CacheSupport clsCache, NameConvert vert, Class<?> cls, String sel);

    /**
     * 删除表结构中数据
     *
     * @param obj 要删除哪张表的数据
     * @param sel 要删除数据的条件
     * @return sql 语句
     */
    String delete(Class<?> obj, NameConvert vert, String sel);

    /**
     * 更新表中数据
     *
     * @param clsCache 通过cls获取查询字段
     * @param cls      要更新的数据
     * @param sel      更新的数据，数据库中的查找条件
     * @return sql 语句
     */
    String updateSql(CacheSupport clsCache, NameConvert vert, Class<?> cls, String sel);

    /**
     * 插入或者更新数据
     *
     * @param clsCache 通过cls获取查询字段
     * @param cls      要插入或更新的数据
     * @param sel      数据库中的查找条件
     * @return sql 语句
     */
    String putSql(CacheSupport clsCache, NameConvert vert, Class<?> cls, String sel);

    /**
     * 插入数据
     *
     * @param clsCache 通过cls获取查询字段
     * @param cls      要插入的数据
     * @return sql 语句
     */
    String addSql(CacheSupport clsCache, NameConvert vert, Class<?> cls);

    /**
     * 创建表结构
     *
     * @param clsCache 通过cls获取查询字段
     * @param cls      要创建的表，获取表名
     * @return sql
     */
    String createTable(CacheSupport clsCache, NameConvert vert, Class<?> cls);

}
