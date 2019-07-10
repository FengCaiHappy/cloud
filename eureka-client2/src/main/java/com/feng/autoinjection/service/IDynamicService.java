package com.feng.autoinjection.service;

public interface IDynamicService {

    String index(Object param, String tableName);

    Object queryById(Object param, String tableName);

    Object list(Object param, String tableName);

    Object update(Object param, String tableName);

    Object delete(Object param, String tableName);

    Object add(Object param, String tableName);
}
