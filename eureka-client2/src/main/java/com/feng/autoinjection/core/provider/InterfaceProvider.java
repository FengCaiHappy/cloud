package com.feng.autoinjection.core.provider;

import com.feng.autoinjection.service.IDynamicService;

import java.lang.reflect.Method;

public interface InterfaceProvider<T> {

    boolean support(Object cls);

    <T> T implMethod(Method method, Object[] args);

    void setDynamicService(IDynamicService dynamicService);
}
