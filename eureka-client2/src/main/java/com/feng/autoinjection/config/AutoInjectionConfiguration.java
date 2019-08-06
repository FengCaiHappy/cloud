package com.feng.autoinjection.config;

import com.feng.autoinjection.Utils.Utils;
import com.feng.autoinjection.controller.IDynamicUrlController;
import com.feng.autoinjection.core.autoinvoker.AutoInvoker;
import com.feng.autoinjection.core.bean.QuickList;
import com.feng.autoinjection.core.bean.TableMapperInfo;
import com.feng.autoinjection.core.provider.InterfaceProvider;
import com.feng.autoinjection.core.provider.impl.DefaultAutoInjectionProvider;
import com.feng.autoinjection.core.resulthandler.IResultHandler;
import com.feng.autoinjection.daoexecutor.IDaoExecutor;
import com.feng.autoinjection.daoexecutor.impl.DefaultDaoExecutor;
import com.feng.autoinjection.mybatisplugin.ReBuildSQLPlugin;
import com.feng.autoinjection.service.IDynamicService;
import com.feng.autoinjection.service.impl.DefaultDynamicService;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.XMLScriptBuilder;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@MapperScan("com.feng.autoinjection.dao")
@ComponentScan("com.feng.autoinjection.config")
public class AutoInjectionConfiguration {

    private Logger logger = LoggerFactory.getLogger(AutoInjectionConfiguration.class);

    private String[] defaultUrls = {"index", "add", "delete", "update", "list", "queryById"};

    @Value("${ftables.xml-location:}")
    private String locationName;

    @Value("${ftables.base-package:}")
    private String basePackage;

    private static final String DEFAULTPACKAGE = "com";

    private static final String XMLTAG = "IsFtable";

    private QuickList<TableMapperInfo> mappers;

    private Map<String, MixedSqlNode> customSQL;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public IDynamicService defaultDynamicService(){
        return new DefaultDynamicService();
    }

    @Bean
    public IDaoExecutor daoExecutor(){
        return new DefaultDaoExecutor(getCustomSQL());
    }

    @Bean
    public ReBuildSQLPlugin reBuildSQLPlugin(){
        return new ReBuildSQLPlugin(getCustomSQL(), getMappers());
    }

    @Bean
    public IDynamicUrlController dynamicUrlController(){
        InterfaceProvider dynamicUrlProvider = new DefaultAutoInjectionProvider(applicationContext, defaultDynamicService(), getMappers(),
                getResultHandler());
        AutoInvoker.setProvider(dynamicUrlProvider);
        return AutoInvoker.getInstance(IDynamicUrlController.class);
    }

    @Bean
    public void setDynamicUrl(){
        List<String> tableNames = Utils.getKeyFromMap(getMappers());
        for(String tableName : tableNames){
            for(int i = 0, len = defaultUrls.length; i < len; i++){
                PatternsRequestCondition patterns = new PatternsRequestCondition("/" + tableName + "/" + defaultUrls[i]);
                logger.info("动态创建URL:" + "/" + tableName + "/" + defaultUrls[i]);
//              RequestMethodsRequestCondition methods = new RequestMethodsRequestCondition(RequestMethod.GET);
                RequestMappingInfo mapping = new RequestMappingInfo(patterns, null, null, null, null, null, null);
                Method method = IDynamicUrlController.class.getDeclaredMethods()[i];

                requestMappingHandlerMapping.registerMapping(mapping, "dynamicUrlController", method);
            }
        }
    }

    private IResultHandler getResultHandler(){
        String[] handlerNames = applicationContext.getBeanNamesForType(IResultHandler.class);
        if(handlerNames.length == 0){
            return null;
        } else {
            for(String names : handlerNames){
                if(!"IResultHandler".equals(names)){
                    return (IResultHandler)applicationContext.getBean(names);
                }
            }
            return null;
        }
    }

    private QuickList getMappers(){
        if(mappers != null){
            return mappers;
        }
        return mappers = Utils.getBeanTableMapper(StringUtils.isEmpty(basePackage) ? DEFAULTPACKAGE:basePackage);
    }

    private void getSqlNode(InputStream inputStream){
        try {
            XPathParser xPathParser = new XPathParser(inputStream);
            XNode allNode = xPathParser.evalNode("/"+XMLTAG);
            org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
            for(XNode xNode : allNode.getChildren()){
                XMLScriptBuilder xmlScriptBuilder = new XMLScriptBuilder(configuration, xNode);
                Method method = XMLScriptBuilder.class.getDeclaredMethod("parseDynamicTags", XNode.class);
                method.setAccessible(true);
                customSQL.put(xNode.getNode().getAttributes().getNamedItem("id").getTextContent(),
                        (MixedSqlNode)method.invoke(xmlScriptBuilder, xNode));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, MixedSqlNode> getCustomSQL(){
        if(customSQL != null){
            return customSQL;
        }
        customSQL = new HashMap<>();
        if(StringUtils.isEmpty(locationName)|| "/".equals(locationName.trim())){
            logger.info("Do no set ftables.xml-location, only can operation single table");
            return customSQL;
        }

        for(Resource resource : getResources(locationName)){
            try {
                getSqlNode(resource.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return customSQL;
    }

    private Resource[] getResources(String location) {
        try {
            return new PathMatchingResourcePatternResolver().getResources(location);
        } catch (IOException e) {
            return new Resource[0];
        }
    }

}