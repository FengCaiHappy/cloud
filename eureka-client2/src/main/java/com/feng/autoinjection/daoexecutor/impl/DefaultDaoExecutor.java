package com.feng.autoinjection.daoexecutor.impl;

import com.feng.autoinjection.dao.DynamicSqlMapper;
import com.feng.autoinjection.daoexecutor.IDaoExecutor;

import javax.annotation.Resource;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class DefaultDaoExecutor implements IDaoExecutor {

    @Resource
    private DynamicSqlMapper dynamicSqlMapper;

    @Override
    public <T> T queryById(Object param, String tableName) {
        return null;
    }

    public <T> T list(Object param, String tableName){
        Map<String, Object> sqlParam = new HashMap<>();
        sqlParam.put("tableName", tableName);
        sqlParam.put("whereSql", beanToWhereSQL(param));
        return (T)dynamicSqlMapper.list(sqlParam);
    }

    @Override
    public <T> T update(Object param, String tableName) {
        return null;
    }

    @Override
    public <T> T delete(Object param, String tableName) {
        return null;
    }

    @Override
    public <T> T add(Object param, String tableName) {
        return null;
    }

    private static String beanToWhereSQL(Object obj)  {
        if(obj == null){
            return null;
        }

        StringBuffer stringBuffer = new StringBuffer(" 1 = 1 ");

        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(obj.getClass());
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {
            String key = property.getName();
            if (key.compareToIgnoreCase("class") == 0) {
                continue;
            }
            Method getter = property.getReadMethod();
            Object value = null;
            try {
                value = getter!=null ? getter.invoke(obj) : null;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            if(value == null){
                continue;
            }
            stringBuffer.append(" And " + key + " = ");
            if("int".equals(getType(value)) || "class java.lang.Integer".equals(getType(value))){
                stringBuffer.append(value);
            } else{
                stringBuffer.append( " '" + value + "' ");
            }

        }
        return stringBuffer.toString();
    }

    private static String getType(Object o){
        return o.getClass().toString();
    }
}
