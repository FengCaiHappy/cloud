package com.feng.autoinjection.core.opratehandler;

import java.util.ArrayList;

public interface IMethodHandler<T> {

    void prepareList(T obj);

    Object afterList(ArrayList<T> obj);

    void prepareQueryById(T obj);

    Object afterQueryById(T obj);

    void prepareUpdate(T obj);

    Object afterUpdate(Integer obj);

    void prepareAdd(T obj);

    Object afterAdd(Integer obj);

    void prepareDelete(T obj);

    Object afterDelete(Integer obj);
}
