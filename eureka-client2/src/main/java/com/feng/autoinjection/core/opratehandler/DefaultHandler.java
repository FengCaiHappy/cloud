package com.feng.autoinjection.core.opratehandler;

import java.util.ArrayList;
import java.util.List;

public class DefaultHandler<T> implements IMethodHandler<T>{


    @Override
    public void prepareList(T obj) {

    }

    @Override
    public <T1> List<T1> afterList(ArrayList<T1> obj) {
        return obj;
    }

    @Override
    public void prepareQueryById(T obj) {

    }

    @Override
    public <T1> T1 afterQueryById(T1 obj) {
        return obj;
    }

    @Override
    public void prepareUpdate(T obj) {

    }

    @Override
    public <T1> T1 afterUpdate(T1 obj) {
        return obj;
    }

    @Override
    public void prepareAdd(T obj) {

    }

    @Override
    public <T1> T1 afterAdd(T1 obj) {
        return obj;
    }

    @Override
    public void prepareDelete(T obj) {

    }

    @Override
    public <T1> T1 afterDelete(T1 obj) {
        return obj;
    }
}
