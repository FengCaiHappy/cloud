package com.feng.autoinjection.config;

import com.feng.autoinjection.core.pagehelperproxy.PageHelperProxy;
import com.feng.autoinjection.mybatisplugin.ReBuildSQLPlugin;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.InterceptorChain;
import org.apache.ibatis.plugin.Intercepts;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import sun.reflect.annotation.AnnotationType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

@Component
public class PageHelperExecutor implements CommandLineRunner {

    @Autowired
    private org.apache.ibatis.session.SqlSessionFactory SqlSessionFactory;

    @Autowired
    private ReBuildSQLPlugin reBuildSQLPlugin;

    @Override
    public void run(String... args) {
        setPageHelperProxy();
    }

    private void setPageHelperProxy(){
        List<Interceptor> interceptors = SqlSessionFactory.getConfiguration().getInterceptors();
        Interceptor pageInterceptor;
        InterceptorChain interceptorChain = new InterceptorChain();
        boolean needReset = false;
        for(Interceptor interceptor : interceptors){
            if("PageInterceptor".equals(interceptor.getClass().getSimpleName())){
                postProcessBeforeInitialization(interceptor);
                pageInterceptor = (Interceptor)new PageHelperProxy(interceptor).getProxyInstance(reBuildSQLPlugin);
                interceptorChain.addInterceptor(pageInterceptor);
                needReset = true;
            } else {
                interceptorChain.addInterceptor(interceptor);
            }
        }
        if(!needReset){
            return;
        }

        Class objClass = SqlSessionFactory.getConfiguration().getClass();
        Field[] fields = objClass.getDeclaredFields();
        for (int i=0;i<fields.length;i++){
            Field field = fields[i];
            field.setAccessible(true);
            String fileName = field.getName();
            if("interceptorChain".equals(fileName)){
                try {
                    field.set(SqlSessionFactory.getConfiguration(), interceptorChain);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Object postProcessBeforeInitialization(Object bean) throws BeansException {
        Intercepts anon = bean.getClass().getAnnotation(Intercepts.class);
        if (anon != null) {
            try {
                InvocationHandler h = Proxy.getInvocationHandler(anon);
                //设置注解支持继承，应对动态代理导致类上的注解丢失
                Field typeField = h.getClass().getDeclaredField("type");
                typeField.setAccessible(true);
                Field annotationTypeField = Class.class.getDeclaredField("annotationType");
                annotationTypeField.setAccessible(true);
                AnnotationType annotationType = (AnnotationType) annotationTypeField.get(typeField.get(h));
                Field inheritedField = AnnotationType.class.getDeclaredField("inherited");
                inheritedField.setAccessible(true);
                inheritedField.set(annotationType, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bean;
    }
}
