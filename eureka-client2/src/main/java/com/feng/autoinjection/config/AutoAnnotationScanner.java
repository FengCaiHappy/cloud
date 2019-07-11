package com.feng.autoinjection.config;

import com.feng.autoinjection.autoannotation.FTableName;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AutoAnnotationScanner extends ClassPathBeanDefinitionScanner {
    private Class type;

    public AutoAnnotationScanner(BeanDefinitionRegistry registry, Class<? extends Annotation> type){
        super(registry,false);
        this.type = type;
    }

    public void registerTypeFilter(){
        addIncludeFilter(new AnnotationTypeFilter(type));
    }

    public static Map<String, Object> getBeanTableMapper(String basePackage){
        Map<String, Object> mappers = new HashMap<>();
        GenericApplicationContext context = new GenericApplicationContext();
        AutoAnnotationScanner autoAnnotationScanner = new AutoAnnotationScanner(context, FTableName.class);
        autoAnnotationScanner.registerTypeFilter();
        autoAnnotationScanner.scan(basePackage);
        context.refresh();
        Map<String, Object> res = context.getBeansWithAnnotation(FTableName.class);
        Iterator<String> iter = res.keySet().iterator();
        while(iter.hasNext()) {
            String key = iter.next();
            Object value = res.get(key);
            Annotation annotation = value.getClass().getAnnotation(FTableName.class);
            Method method = null;
            try {
                method = annotation.annotationType().getDeclaredMethod("name", null);
                if(!method.isAccessible()){
                    method.setAccessible(true);
                }
                mappers.put(method.invoke(annotation, null).toString(), value.getClass().getName());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return mappers;
    }
}
