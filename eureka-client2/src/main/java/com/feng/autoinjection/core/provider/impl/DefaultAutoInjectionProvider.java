package com.feng.autoinjection.core.provider.impl;

import com.alibaba.fastjson.JSONObject;
import com.feng.autoinjection.Utils.Utils;
import com.feng.autoinjection.controller.IDynamicUrlController;
import com.feng.autoinjection.core.provider.InterfaceProvider;
import com.feng.autoinjection.core.resulthandler.IResultHandler;
import com.feng.autoinjection.service.IDynamicService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class DefaultAutoInjectionProvider implements InterfaceProvider {

    private IDynamicService dynamicService;

    private Map<String, Object> mappers;
    private IResultHandler handler;

    public DefaultAutoInjectionProvider(){
        super();
    }

    public DefaultAutoInjectionProvider(Map<String, Object> mappers, IResultHandler handler){
        this();
        this.mappers = mappers;
        this.handler = handler;
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
        String tableName = patterns[1], methodName = patterns[2];
        Map<String, Object> params = Utils.getParameterMap(request);
        try {
            Object beanParam = JSONObject.parseObject(JSONObject.toJSONString(params), Class.forName(getFullBeanName(tableName)));
            Method invokeMethod = IDynamicService.class.getDeclaredMethod(methodName, Object.class, String.class);
            Object result = invokeMethod.invoke(dynamicService, beanParam, tableName);
            if(handler != null){
               return handler.handler(result);
            }
            return result;
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

}
