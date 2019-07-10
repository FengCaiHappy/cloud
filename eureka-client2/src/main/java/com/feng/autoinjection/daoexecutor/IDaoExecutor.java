package com.feng.autoinjection.daoexecutor;

public interface IDaoExecutor {

    <T> T queryById(Object param, String tableName);

    <T> T list(Object param, String tableName);

    <T> T update(Object param, String tableName);

    <T> T delete(Object param, String tableName);

    <T> T add(Object param, String tableName);
}
