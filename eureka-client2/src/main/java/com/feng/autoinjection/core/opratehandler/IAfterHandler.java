package com.feng.autoinjection.core.opratehandler;

public interface IAfterHandler<T> {
    <T> T afterList(T obj);

    <T> T afterQueryById(T obj);

    <T> T afterUpdate(T obj);

    <T> T afterAdd(T obj);

    <T> T afterDelete(T obj);
}
