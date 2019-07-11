package com.feng.autoinjection.config;

import com.feng.autoinjection.Utils.Utils;
import com.feng.autoinjection.controller.IDynamicUrlController;
import com.feng.autoinjection.core.autoinvoker.AutoInvoker;
import com.feng.autoinjection.core.provider.InterfaceProvider;
import com.feng.autoinjection.core.provider.impl.DefaultAutoInjectionProvider;
import com.feng.autoinjection.daoexecutor.IDaoExecutor;
import com.feng.autoinjection.daoexecutor.impl.DefaultDaoExecutor;
import com.feng.autoinjection.service.IDynamicService;
import com.feng.autoinjection.service.impl.DefaultDynamicService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;


@Configuration
public class AutoInjectionWebConfiguration {

    private Logger logger = LoggerFactory.getLogger(AutoInjectionWebConfiguration.class);

    private String[] defaultUrls = {"index", "add", "delete", "update", "list", "queryById"};

    private Map<String, Object> mappers;

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

    @Bean
    public IDynamicUrlController dynamicUrlController(){
        InterfaceProvider dynamicUrlProvider = new DefaultAutoInjectionProvider(getMappers());
        dynamicUrlProvider.setDynamicService(defaultDynamicService());
        AutoInvoker.setProvider(dynamicUrlProvider);
        return AutoInvoker.getInstance(IDynamicUrlController.class);
    }

    @Bean
    public void setDynamicUrl(){
        Map<String, Object> mappers = getMappers();
        List<String> tableNames = Utils.getKeyFromMap(mappers);
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

    private Map<String, Object> getMappers(){
        if(mappers != null){
            return mappers;
        }
        String basePackage = StringUtils.isEmpty(Utils.getYMLProperties("fTableBasePackage")) ? "com.feng":
                Utils.getYMLProperties("fTableBasePackage");
        return mappers = AutoAnnotationScanner.getBeanTableMapper(basePackage);
    }
}