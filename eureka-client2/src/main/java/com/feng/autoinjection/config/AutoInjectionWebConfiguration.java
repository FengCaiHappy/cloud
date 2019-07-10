package com.feng.autoinjection.config;

import com.feng.autoinjection.controller.IDynamicUrlController;
import com.feng.autoinjection.core.autoinvoker.AutoInvoker;
import com.feng.autoinjection.core.provider.InterfaceProvider;
import com.feng.autoinjection.core.provider.impl.DefaultAutoInjectionProvider;
import com.feng.autoinjection.daoexecutor.IDaoExecutor;
import com.feng.autoinjection.daoexecutor.impl.DefaultDaoExecutor;
import com.feng.autoinjection.service.IDynamicService;
import com.feng.autoinjection.service.impl.DefaultDynamicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Method;
import java.util.List;


@Configuration
public class AutoInjectionWebConfiguration {

    Logger logger = LoggerFactory.getLogger(AutoInjectionWebConfiguration.class);

    @Value("#{'${fTable.names}'.split(',')}")
    private List<String> tableNames;

    private String[] defaultUrls = {"index", "add", "delete", "update", "list", "queryById"};

    @Autowired
    private AbstractHandlerMethodMapping abstractHandlerMethodMapping;


    @Bean
    public IDynamicService defaultDynamicService(){
        return new DefaultDynamicService();
    }

    @Bean
    public IDaoExecutor daoExecutor(){
        return new DefaultDaoExecutor();
    }

//    @Bean
//    public MapperScannerConfigurer aaa(){
//        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
//        mapperScannerConfigurer.setBasePackage("com.feng.autoinjection.dao");
//        return mapperScannerConfigurer;
//    }

    @Bean
    public IDynamicUrlController dynamicUrlController(){
        InterfaceProvider dynamicUrlProvider = new DefaultAutoInjectionProvider();
        dynamicUrlProvider.setDynamicService(defaultDynamicService());
        AutoInvoker.setProvider(dynamicUrlProvider);
        return AutoInvoker.getInstance(IDynamicUrlController.class);
    }

    @Bean
    public void setDynamicUrl(){
        for(String tableName : tableNames){
            for(int i = 0, len = defaultUrls.length; i < len; i++){
                PatternsRequestCondition patterns = new PatternsRequestCondition("/" + tableName + "/" + defaultUrls[i]);
                logger.info("动态创建URL:" + "/" + tableName + "/" + defaultUrls[i]);
//              RequestMethodsRequestCondition methods = new RequestMethodsRequestCondition(RequestMethod.GET);
                RequestMappingInfo mapping = new RequestMappingInfo(patterns, null, null, null, null, null, null);
                Method method = IDynamicUrlController.class.getDeclaredMethods()[i];

                abstractHandlerMethodMapping.registerMapping(mapping, "dynamicUrlController", method);
            }
        }
    }
}
