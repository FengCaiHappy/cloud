package com.feng.autoinjection.core.provider.impl;

import com.alibaba.fastjson.JSONObject;
import com.feng.autoinjection.Utils.Utils;
import com.feng.autoinjection.controller.IDynamicUrlController;
import com.feng.autoinjection.core.bean.QuickList;
import com.feng.autoinjection.core.opratehandler.IMethodHandler;
import com.feng.autoinjection.core.provider.InterfaceProvider;
import com.feng.autoinjection.core.resulthandler.IResultHandler;
import com.feng.autoinjection.service.IDynamicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;

public class DefaultAutoInjectionProvider implements InterfaceProvider {

    private Logger logger = LoggerFactory.getLogger(DefaultAutoInjectionProvider.class);

    private IDynamicService dynamicService;

    private QuickList mappers;
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

            IMethodHandler methodHandler = (IMethodHandler)getHandlerBean(tableName);
            if(methodHandler != null){
                Method prepareMethod = IMethodHandler.class.getDeclaredMethod("prepare"+ Utils.upperFirst(methodName), Object.class);
                prepareMethod.invoke(methodHandler, beanParam);
            }

            Object result = invokeMethod.invoke(dynamicService, beanParam, tableName);

            if(methodHandler != null){
                Method afterMethod = IMethodHandler.class.getDeclaredMethod("after"+ Utils.upperFirst(methodName), result.getClass());
                afterMethod.invoke(methodHandler, result);
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

    private Object getHandlerBean(String tableName){
        String name = mappers.getBean(tableName).getMethodHandlerName();
        Object handlerBean = null;
        try{
            handlerBean = applicationContext.getBean(name);
        }catch (Exception e){
            logger.info("can not found method handler");
        }
        return handlerBean;
    }

    private String getFullBeanName(String beanName){
        return mappers.getBean(beanName).getMapperBeaName();
    }

}
