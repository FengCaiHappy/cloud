package com.feng.autoinjection.dao;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface DynamicSqlMapper<T> {

    @Select("select * from ${tableName} where ${whereSql};")
    T queryById(Map<String, Object> param);

    @Select("select * from ${tableName} where ${whereSql};")
    List<Map> list(Map<String, Object> param);

    @Update("update ${tableName} set ${columnName} where ${whereSql};")
    Integer update(Map<String, Object> param);

    @Delete("delete from ${tableName} where ${whereSql};")
    Integer delete(Map<String, Object> param);

    @Insert("insert into ${tableName}( ${columnName} ) values ( ${columnValue} );")
    Integer add(Map<String, Object> param);
}
