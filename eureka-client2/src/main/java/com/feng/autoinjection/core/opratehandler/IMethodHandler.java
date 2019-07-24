package com.feng.autoinjection.core.opratehandler;

import java.util.ArrayList;
import java.util.List;

public interface IMethodHandler<T> {

    void prepareList(T obj);

    List<T> afterList(ArrayList<T> obj);

    void prepareQueryById(T obj);

    T afterQueryById(T obj);

    void prepareUpdate(T obj);

    T afterUpdate(T obj);

    void prepareAdd(T obj);

    T afterAdd(T obj);

    void prepareDelete(T obj);

    T afterDelete(T obj);
}
