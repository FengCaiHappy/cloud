package com.feng.autoinjection.core.opratehandler;

public interface IPrepareHandler<T> {
    <T> T prepareList(T obj);

    <T> T prepareQueryById(T obj);

    <T> T prepareUpdate(T obj);

    <T> T prepareAdd(T obj);

    <T> T prepareDelete(T obj);
}
