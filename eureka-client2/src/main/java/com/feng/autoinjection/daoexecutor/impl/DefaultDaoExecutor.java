package com.feng.autoinjection.daoexecutor.impl;

import com.feng.autoinjection.Utils.Utils;
import com.feng.autoinjection.dao.DynamicSqlMapper;
import com.feng.autoinjection.daoexecutor.IDaoExecutor;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.springframework.util.StringUtils;

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

    private Map<String, MixedSqlNode> multiTableQuerySQL;

    public DefaultDaoExecutor(){
        super();
    }

    public DefaultDaoExecutor(Map<String, MixedSqlNode> multiTableQuerySQL){
        this();
        this.multiTableQuerySQL = multiTableQuerySQL;
    }

    //todo id?

    @Override
    public <T> T queryById(Object param, String tableName) {
        if(multiTableQuerySQL.get(tableName + ".queryById") != null){
            Map<String, Object> paramMap = Utils.beanTOMap(param);
            paramMap.put("tableName", tableName);
            return (T)dynamicSqlMapper.queryById(paramMap);
        }
        Map<String, Object> sqlParam = new HashMap<>();
        sqlParam.put("tableName", tableName);
        sqlParam.put("whereSql", beanToSQL(param).get("whereSQL"));
        return (T)dynamicSqlMapper.queryById(sqlParam);
    }

    public <T> T list(Object param, String tableName){
        if(multiTableQuerySQL.get(tableName + ".list") != null){
            Map<String, Object> paramMap = Utils.beanTOMap(param);
            paramMap.put("tableName", tableName);
            return (T)dynamicSqlMapper.list(paramMap);
        }
        Map<String, Object> sqlParam = new HashMap<>();
        sqlParam.put("tableName", tableName);
        sqlParam.put("whereSql", beanToSQL(param).get("whereSQL"));
        return (T)dynamicSqlMapper.list(sqlParam);
    }

    @Override
    public <T> T update(Object param, String tableName) {
        Map<String, Object> sqlParam = new HashMap<>();
        sqlParam.put("tableName", tableName);
        Map<String, String> sqls = beanToSQL(param);
        String whereSql = sqls.get("updateWhereSQL");
        if(StringUtils.isEmpty(whereSql)){
            throw new NullPointerException("can not found id, please check your params");
        }
        sqlParam.put("columnName", sqls.get("updateSQL"));
        sqlParam.put("whereSql", whereSql);
        return (T)dynamicSqlMapper.update(sqlParam);
    }

    @Override
    public <T> T delete(Object param, String tableName) {
        Map<String, Object> sqlParam = new HashMap<>();
        sqlParam.put("tableName", tableName);
        Map<String, String> sqls = beanToSQL(param);
        if("0".equals(sqls.get("valueCount"))){
            throw new NullPointerException("can not found any param values, please check your params");
        }
        sqlParam.put("whereSql", sqls.get("whereSQL"));
        return (T)dynamicSqlMapper.delete(sqlParam);
    }

    @Override
    public <T> T add(Object param, String tableName) {
        Map<String, Object> sqlParam = new HashMap<>();
        sqlParam.put("tableName", tableName);
        Map<String, String> sqls = beanToSQL(param);
        sqlParam.put("columnName", sqls.get("keySet"));
        sqlParam.put("columnValue", sqls.get("valueSet"));
        return (T)dynamicSqlMapper.add(sqlParam);
    }

    //todo better
    private static Map<String, String> beanToSQL(Object obj) {
        if(obj == null){
            return null;
        }
        Map<String, String> resultMap = new HashMap<>();

        Integer valueCount = 0;
        StringBuffer whereSQL = new StringBuffer(" 1 = 1 ");
        StringBuffer keySet = new StringBuffer(" ");
        StringBuffer valueSet = new StringBuffer(" ");
        StringBuffer updateSQL = new StringBuffer(" ");
        StringBuffer updateWhereSQL = new StringBuffer();

        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(obj.getClass());
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

        boolean firstIndex = true;

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
            valueCount++;
            if(!firstIndex){
                keySet.append(" , ");
                valueSet.append(" , ");
                updateSQL.append(" , ");
            }

            keySet.append(" " + key + " ");
            updateSQL.append(" " + key + " = ");
            whereSQL.append(" And " + key + " = ");
            if("int".equals(getType(value)) || "class java.lang.Integer".equals(getType(value))){
                whereSQL.append(value);
                valueSet.append(" " + value + " ");
                updateSQL.append(" " + value + " ");
                if("id".equals(key)){
                    updateWhereSQL.append("id = " + value);
                }
            } else{
                whereSQL.append( " '" + value + "' ");
                valueSet.append(" '" + value + "' ");
                updateSQL.append(" '" + value + "' ");
                if("id".equals(key)){
                    updateWhereSQL.append("id = '" + value + "' ");
                }
            }
            firstIndex = false;
        }
        resultMap.put("whereSQL", whereSQL.toString());
        resultMap.put("keySet", keySet.toString());
        resultMap.put("valueSet", valueSet.toString());
        resultMap.put("updateSQL", updateSQL.toString());
        resultMap.put("updateWhereSQL", updateWhereSQL.toString());
        resultMap.put("valueCount", valueCount.toString());
        return resultMap;
    }

    private static String getType(Object o){
        return o.getClass().toString();
    }
}
