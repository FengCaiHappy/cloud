package com.feng.autoinjection.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface DynamicSqlMapper {

    Object queryById(Map<String, Object> param);

    List<Object> list(Map<String, Object> param);

    Object update(Map<String, Object> param);

    Object delete(Map<String, Object> param);

    Object add(Map<String, Object> param);
}
