package com.feng.autoinjection.core.opratehandler;

public class DefaultHandler<T> implements IPrepareHandler<T>, IAfterHandler<T>{

    @Override
    public <T> T prepareList(T obj) {
        return obj;
    }

    @Override
    public <T> T prepareQueryById(T obj) {
        return obj;
    }

    @Override
    public <T> T prepareUpdate(T obj) {
        return obj;
    }

    @Override
    public <T> T prepareAdd(T obj) {
        return obj;
    }

    @Override
    public <T> T prepareDelete(T obj) {
        return obj;
    }

    @Override
    public <T> T afterList(T obj) {
        return obj;
    }

    @Override
    public <T> T afterQueryById(T obj) {
        return obj;
    }

    @Override
    public <T> T afterUpdate(T obj) {
        return obj;
    }

    @Override
    public <T> T afterAdd(T obj) {
        return obj;
    }

    @Override
    public <T> T afterDelete(T obj) {
        return obj;
    }
}
