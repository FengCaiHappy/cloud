package com.feng.autoinjection.core.opratehandler;

import java.util.ArrayList;
import java.util.List;

public class DefaultHandler<T> implements IMethodHandler<T>{


    @Override
    public void prepareList(T obj) {

    }

    @Override
    public List<T> afterList(ArrayList<T> obj) {
        return obj;
    }

    @Override
    public void prepareQueryById(T obj) {

    }

    @Override
    public T afterQueryById(T obj) {
        return obj;
    }

    @Override
    public void prepareUpdate(T obj) {

    }

    @Override
    public T afterUpdate(T obj) {
        return obj;
    }

    @Override
    public void prepareAdd(T obj) {

    }

    @Override
    public T afterAdd(T obj) {
        return obj;
    }

    @Override
    public void prepareDelete(T obj) {

    }

    @Override
    public T afterDelete(T obj) {
        return obj;
    }
}
