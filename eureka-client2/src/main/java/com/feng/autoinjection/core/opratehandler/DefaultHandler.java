package com.feng.autoinjection.core.opratehandler;

import java.util.ArrayList;

public class DefaultHandler<T> implements IMethodHandler<T>{


    @Override
    public void prepareIndex(T obj) {

    }

    @Override
    public String afterIndex(Object obj) {
        return obj.toString();
    }

    @Override
    public void prepareList(T obj) {

    }

    @Override
    public Object afterList(ArrayList<T> obj) {
        return obj;
    }

    @Override
    public void prepareQueryById(T obj) {

    }

    @Override
    public Object afterQueryById(T obj) {
        return obj;
    }

    @Override
    public void prepareUpdate(T obj) {

    }

    @Override
    public Object afterUpdate(Integer obj) {
        return obj;
    }

    @Override
    public void prepareAdd(T obj) {

    }

    @Override
    public Object afterAdd(Integer obj) {
        return obj;
    }

    @Override
    public void prepareDelete(T obj) {

    }

    @Override
    public Object afterDelete(Integer obj) {
        return obj;
    }
}
