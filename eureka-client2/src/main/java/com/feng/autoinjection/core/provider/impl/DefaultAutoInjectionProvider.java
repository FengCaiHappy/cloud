package com.feng.autoinjection.core.provider.impl;

import com.alibaba.fastjson.JSONObject;
import com.feng.autoinjection.controller.IDynamicUrlController;
import com.feng.autoinjection.core.provider.InterfaceProvider;
import com.feng.autoinjection.service.IDynamicService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DefaultAutoInjectionProvider implements InterfaceProvider {

    private IDynamicService dynamicService;

    private Map<String, Object> mappers;

    public DefaultAutoInjectionProvider(){

    }

    public DefaultAutoInjectionProvider(Map<String, Object> mappers){
        this.mappers = mappers;
    }

    public void setDynamicService(IDynamicService dynamicService){
        this.dynamicService = dynamicService;
    }

    @Override
    public boolean support(Object cls) {
        return IDynamicUrlController.class == cls;
    }

    @Override
    public Object implMethod(Method method, Object[] args) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String url = request.getRequestURI();
        String[] patterns = url.split("/");
        if(patterns == null || patterns.length < 2){
            return null;
        }
        String tableName = patterns[1];
        String methodName = patterns[2];
        Map<String, Object> params = getParameterMap(request);
        try {
            Object beanParam = JSONObject.parseObject(JSONObject.toJSONString(params), Class.forName(getFullBeanName(tableName)));
            Method invokeMethod = IDynamicService.class.getDeclaredMethod(methodName, Object.class, String.class);
            return invokeMethod.invoke(dynamicService, beanParam, tableName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }  catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getFullBeanName(String beanName){
        return mappers.get(beanName).toString();
    }

    private static Map<String, Object> getParameterMap(HttpServletRequest request) {
        Map<String, String[]> properties = request.getParameterMap();
        Map<String, Object> returnMap = new HashMap<>();
        Iterator<Map.Entry<String, String[]>> iter = properties.entrySet().iterator();
        String name;
        String value = "";
        while (iter.hasNext()) {
            Map.Entry<String, String[]> entry = iter.next();
            name = entry.getKey();
            Object valueObj = entry.getValue();
            if (null == valueObj) {
                value = "";
            } else if (valueObj instanceof String[]) {
                String[] values = (String[]) valueObj;
                for (int i = 0; i < values.length; i++) {
                    value = values[i] + ",";
                }
                value = value.substring(0, value.length() - 1);
            } else {
                value = valueObj.toString();
            }
            returnMap.put(name, value);
        }
        return returnMap;
    }
}
