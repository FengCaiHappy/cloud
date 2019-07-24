package com.feng.autoinjection.config;

import com.feng.autoinjection.Utils.Utils;
import com.feng.autoinjection.controller.IDynamicUrlController;
import com.feng.autoinjection.core.autoinvoker.AutoInvoker;
import com.feng.autoinjection.core.bean.QuickList;
import com.feng.autoinjection.core.provider.InterfaceProvider;
import com.feng.autoinjection.core.provider.impl.DefaultAutoInjectionProvider;
import com.feng.autoinjection.core.resulthandler.IResultHandler;
import com.feng.autoinjection.daoexecutor.IDaoExecutor;
import com.feng.autoinjection.daoexecutor.impl.DefaultDaoExecutor;
import com.feng.autoinjection.mybatisplugin.ReBuildSQLPlugin;
import com.feng.autoinjection.service.IDynamicService;
import com.feng.autoinjection.service.impl.DefaultDynamicService;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

    private QuickList mappers;

    private Map<String, List<SqlNode>> multiTableQuerySQL;

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
        getMultiTableQuerySQL();
        return new DefaultDaoExecutor(multiTableQuerySQL);
    }

    @Bean
    public ReBuildSQLPlugin reBuildSQLPlugin(){
        getMultiTableQuerySQL();
        return new ReBuildSQLPlugin(multiTableQuerySQL);
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

    public void getMultiTableQuerySQL(){
        if(multiTableQuerySQL != null){
            return;
        }
        if(StringUtils.isEmpty(locationName)|| "/".equals(locationName.trim())){
            logger.info("Do no set ftables.xml-location, only can operation single table");
            multiTableQuerySQL = new HashMap<>();
            return;
        }

        List<File> result = new ArrayList<>();
        searchFiles(getResourcesFile(), getLocationNameArr(locationName), 0, result);

        SAXReader reader = new SAXReader();

        multiTableQuerySQL = new HashMap<>();

        for(File file : result){
            Document document = null;
            try {
                document = reader.read(file);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            Element rootElement = document.getRootElement();
            if(XMLTAG.equals(rootElement.getName())){
                Iterator iterator = rootElement.elementIterator();
                while(iterator.hasNext()){
                    List<SqlNode> list = new ArrayList<>();
                    String key = "";
                    String orderBy = "";
                    Element childElement = (Element) iterator.next();
                    List<Attribute> attributes = childElement.attributes();
                    for(Attribute attribute : attributes){
                        //"tagName:" + childElement.getName()
                        key = attribute.getValue();
                        String text = childElement.getTextTrim();
                        String orderByStr = "order by";
                        if(text.contains(orderByStr)){
                            orderBy = text.substring(text.indexOf(orderByStr), text.length());
                            text = text.substring(0, text.indexOf(orderByStr));
                        }
                        StaticTextSqlNode staticTextSqlNode = new StaticTextSqlNode(text);
                        list.add(staticTextSqlNode);
                    }
                    Iterator childIterator = childElement.elementIterator();
                    while (childIterator.hasNext()){
                        Element nodeElement = (Element)childIterator.next();
                        List<Attribute> nodeAttriutes = nodeElement.attributes();
                        String nodeKey = "";
                        for(Attribute attribute : nodeAttriutes){
                            nodeKey = attribute.getValue();
                        }
                        StaticTextSqlNode staticTextSqlNode = new StaticTextSqlNode(nodeElement.getTextTrim());
                        List<SqlNode> sqlNodeList = new ArrayList<>();
                        sqlNodeList.add(staticTextSqlNode);
                        MixedSqlNode mixedSqlNode = new MixedSqlNode(sqlNodeList);
                        IfSqlNode ifSqlNode = new IfSqlNode(mixedSqlNode, nodeKey);
                        list.add(ifSqlNode);
                        StaticTextSqlNode sqlNode = new StaticTextSqlNode(" ");
                        list.add(sqlNode);
                    }
                    if(!StringUtils.isEmpty(orderBy)){
                        StaticTextSqlNode sqlNode = new StaticTextSqlNode(orderBy);
                        list.add(sqlNode);
                    }
                    multiTableQuerySQL.put(key, list);
                }
            }
        }
    }

    private File getResourcesFile(){
        String classPath = this.getClass().getResource("/").getPath();
        File rootFile = new File(classPath).getParentFile();
        File[] files = rootFile.listFiles();
        File result = null;
        for(File file : files){
            if(file.getName().contains("resources")){
                result = file;
            }
        }

        if(result == null){
            result = new File(classPath);
        }
        return result;
    }

    private static String[] getLocationNameArr(String locationName){
        if(locationName.startsWith("/")){
            locationName = locationName.substring(1, locationName.length());
        }
        return locationName.split("/");
    }

    public static void searchFiles(File rootFile, String[] path, int level, List<File> result) {
        File[] files = rootFile.listFiles((FileFilter) new WildcardFileFilter(path[level]));
        if(files.length > 0){
            for(File file : files){
                if(file.isDirectory()){
                    searchFiles(file, path, level+1, result);
                } else{
                    result.add(file);
                }
            }
        }
    }

}