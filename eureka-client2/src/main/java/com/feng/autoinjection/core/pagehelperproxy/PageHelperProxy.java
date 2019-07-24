package com.feng.autoinjection.core.pagehelperproxy;

import com.feng.autoinjection.mybatisplugin.ReBuildSQLPlugin;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class PageHelperProxy implements MethodInterceptor {
    //维护目标对象
    private Object target;

    public PageHelperProxy(Object target) {
        this.target = target;
    }

    private ReBuildSQLPlugin reBuildSQLPlugin;

    //给目标对象创建一个代理对象
    public Object getProxyInstance(ReBuildSQLPlugin reBuildSQLPlugin){
        this.reBuildSQLPlugin = reBuildSQLPlugin;
        //1.工具类
        Enhancer en = new Enhancer();
        //2.设置父类
        en.setSuperclass(target.getClass());
        //3.设置回调函数
        en.setCallback(this);
        //4.创建子类(代理对象)
        return en.create();

    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        if("plugin".equals(method.getName())){
            return Plugin.wrap(args[0], (Interceptor) obj);
        }
        if("intercept".equals(method.getName())){
            Invocation invocation = (Invocation)args[0];
            reBuildSQLPlugin.intercept(invocation);
        }
        return method.invoke(target, args);
    }
}
