package com.feng.autoinjection.core.provider.impl;

import com.alibaba.fastjson.JSONObject;
import com.feng.autoinjection.Utils.Utils;
import com.feng.autoinjection.controller.IDynamicUrlController;
import com.feng.autoinjection.core.opratehandler.IAfterHandler;
import com.feng.autoinjection.core.opratehandler.IPrepareHandler;
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

    private Map<String, Object> mappers;
    private Map<String, Object> afterMappers;
    private Map<String, Object> prepareMappers;
    private IResultHandler handler;
    private ApplicationContext applicationContext;

    public DefaultAutoInjectionProvider(){
        super();
    }

    public DefaultAutoInjectionProvider(IDynamicService dynamicService, Map<String, Object> mappers, IResultHandler handler){
        this();
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

            //todo before
            IPrepareHandler prepareHandler = (IPrepareHandler)applicationContext.getBean("");
            if(prepareHandler != null){
                Method prepareMethod = IPrepareHandler.class.getDeclaredMethod(methodName, Object.class);
                prepareMethod.invoke(prepareHandler, beanParam);
            }

            Object result = invokeMethod.invoke(dynamicService, beanParam, tableName);

            //todo after
            IAfterHandler afterHandler = (IAfterHandler)applicationContext.getBean("");
            if(afterHandler != null){
                Method afterMethod = IAfterHandler.class.getDeclaredMethod(methodName, Object.class);
                afterMethod.invoke(afterHandler, result);
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

    private String getFullBeanName(String beanName){
        return mappers.get(beanName).toString();
    }

}
