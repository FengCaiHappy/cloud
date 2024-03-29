package com.feng.autoinjection.core.provider.impl;

import com.feng.autoinjection.Utils.Utils;
import com.feng.autoinjection.controller.IDynamicUrlController;
import com.feng.autoinjection.core.bean.QuickList;
import com.feng.autoinjection.core.bean.TableMapperInfo;
import com.feng.autoinjection.core.opratehandler.IMethodHandler;
import com.feng.autoinjection.core.provider.InterfaceProvider;
import com.feng.autoinjection.core.resulthandler.IResultHandler;
import com.feng.autoinjection.service.IDynamicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultAutoInjectionProvider implements InterfaceProvider {

    private Logger logger = LoggerFactory.getLogger(DefaultAutoInjectionProvider.class);

    private IDynamicService dynamicService;

    private QuickList<TableMapperInfo> mappers;
    private IResultHandler handler;
    private ApplicationContext applicationContext;

    public DefaultAutoInjectionProvider(){
        super();
    }

    public DefaultAutoInjectionProvider(ApplicationContext applicationContext, IDynamicService dynamicService, QuickList mappers, IResultHandler handler){
        this();
        this.applicationContext = applicationContext;
        this.dynamicService = dynamicService;
        this.mappers = mappers;
        this.handler = handler;
    }

    @Override
    public boolean support(Object cls) {
        return IDynamicUrlController.class == cls;
    }

    @Override
    public Object implMethod(Method method, Object[] args) {
        long startTime = System.currentTimeMillis();
        HttpServletRequest request = Utils.getHttpRequest();
        String url = request.getServletPath();
        String[] patterns = url.split("/");
        if(patterns == null || patterns.length < 2){
            return null;
        }
        String tableName = getTableName(patterns[1]), methodName = patterns[2];
        Map<String, Object> params = Utils.getParameterMap(request);
        try {
            Object beanParam = Utils.mapToBean(params, Class.forName(getBeanFullName(tableName)));
            Method invokeMethod = IDynamicService.class.getDeclaredMethod(methodName, Object.class, String.class);

            IMethodHandler methodHandler = (IMethodHandler)getHandlerBean(tableName);
            if(methodHandler != null){
                Method prepareMethod = IMethodHandler.class.getDeclaredMethod("prepare"+ Utils.upperFirst(methodName), Object.class);
                prepareMethod.invoke(methodHandler, beanParam);
            }

            Object result = invokeMethod.invoke(dynamicService, beanParam, tableName);

            if(methodHandler != null){
                Method afterMethod = IMethodHandler.class.getDeclaredMethod("after"+ Utils.upperFirst(methodName), getResultClass(result));
                result = afterMethod.invoke(methodHandler, result);
            }

            //全局结果处理器
            if(handler != null){
               return handler.handler(result);
            }
            logger.info("调用耗时: " + (System.currentTimeMillis()-startTime) + "MS");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private Class getResultClass(Object result){
        Class clazz;
        if(result instanceof List){
            clazz = ArrayList.class;
        } else if(result instanceof Integer){
            clazz = Integer.class;
        } else {
            clazz = Object.class;
        }
        return clazz;
    }

    private Object getHandlerBean(String tableName){
        String name = mappers.getBean(tableName).getMethodHandlerName();
        try{
            return applicationContext.getBean(name);
        }catch (Exception e){
            logger.info("can not found method handler");
        }
        return null;
    }

    private String getBeanFullName(String tableName){
        return mappers.getBean(tableName).getMapperBeaName();
    }

    private String getTableName(String urlName){
        return mappers.getBean(urlName).getTableName();
    }
}
