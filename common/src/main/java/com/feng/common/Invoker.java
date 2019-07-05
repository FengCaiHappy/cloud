package com.feng.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public final class Invoker {

    private Invoker(){}

    private static final List<InterfaceProvider> providers = new ArrayList<>();

    public static void setProvider(InterfaceProvider provider){
        providers.add(provider);
    }

    public static <T> T getInstance(Class<T> cls){
        MethodProxy invocationHandler = new MethodProxy(cls);
        return (T)Proxy.newProxyInstance(cls.getClassLoader(),
                new Class[]{cls},
                invocationHandler);
    }

    static class MethodProxy implements InvocationHandler {

        private Class<?> classType;

        public MethodProxy(Class<?> classType){
            super();
            this.classType = classType;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //如果传进来是一个已实现的具体类
            if(Object.class.equals(method.getDeclaringClass())){
                return method.invoke(this, args);
            }
            //如果传进来的是一个接口
            else {
                for(InterfaceProvider provider : providers){
                    if(provider.support(classType)){
                        return provider.implMethod(method, args);
                    }
                }
                throw new NullPointerException("找不到对应的动态实现支持");
            }
        }
    }

//    private static class SingleTonHoler{
//        private static final Invoker INSTANCE = new Invoker();
//    }
//
//    public static Invoker getInstance(){
//        return SingleTonHoler.INSTANCE;
//    }
}