package com.feng.autoinjection.core.opratehandler;

import java.util.ArrayList;
import java.util.List;

public interface IMethodHandler<T> {

    void prepareList(T obj);

    <T> List<T> afterList(ArrayList<T> obj);

    void prepareQueryById(T obj);

    <T> T afterQueryById(T obj);

    void prepareUpdate(T obj);

    <T> T afterUpdate(T obj);

    void prepareAdd(T obj);

    <T> T afterAdd(T obj);

    void prepareDelete(T obj);

    <T> T afterDelete(T obj);
}
