package com.feng.autoinjection.core.provider;

import java.lang.reflect.Method;

public interface InterfaceProvider<T> {

    boolean support(Object cls);

    <T> T implMethod(Method method, Object[] args);
}
