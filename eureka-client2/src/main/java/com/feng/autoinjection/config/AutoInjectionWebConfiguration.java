package com.feng.autoinjection.config;

import com.feng.autoinjection.Utils.Utils;
import com.feng.autoinjection.controller.IDynamicUrlController;
import com.feng.autoinjection.core.autoinvoker.AutoInvoker;
import com.feng.autoinjection.core.provider.InterfaceProvider;
import com.feng.autoinjection.core.provider.impl.DefaultAutoInjectionProvider;
import com.feng.autoinjection.core.resulthandler.IResultHandler;
import com.feng.autoinjection.daoexecutor.IDaoExecutor;
import com.feng.autoinjection.daoexecutor.impl.DefaultDaoExecutor;
import com.feng.autoinjection.mybatisplugin.ReBuildSQLPlugin;
import com.feng.autoinjection.service.IDynamicService;
import com.feng.autoinjection.service.impl.DefaultDynamicService;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.util.*;


@Configuration
public class AutoInjectionWebConfiguration {

    private Logger logger = LoggerFactory.getLogger(AutoInjectionWebConfiguration.class);

    private String[] defaultUrls = {"index", "add", "delete", "update", "list", "queryById"};

    @Value("${ftables.xml-location}")
    private String locationName;

    @Value("${ftables.base-package}")
    private String basePackage;

    private static final String DEFAULTPACKAGE = "com";

    private static final String XMLTAG = "IsFtable";

    private Map<String, Object> mappers;

    private Map<String, String> multiTableQuerySQL;

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
    public ReBuildSQLPlugin DemoPlugin(){
        getMultiTableQuerySQL();
        return new ReBuildSQLPlugin(multiTableQuerySQL);
    }

    @Bean
    public IDynamicUrlController dynamicUrlController(){
        InterfaceProvider dynamicUrlProvider = new DefaultAutoInjectionProvider(getMappers(), getResultHandler());
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

    private Map<String, Object> getMappers(){
        if(mappers != null){
            return mappers;
        }
        return mappers = AutoAnnotationScanner.getBeanTableMapper(StringUtils.isEmpty(basePackage) ? DEFAULTPACKAGE:basePackage);
    }

    public void getMultiTableQuerySQL(){
        if(multiTableQuerySQL != null){
            return;
        }
        if(StringUtils.isEmpty(locationName)|| "/".equals(locationName.trim())){
            throw new NullPointerException("the path is null");
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
                    Element childElement = (Element) iterator.next();
                    List<Attribute> attributes = childElement.attributes();
                    for(Attribute attribute : attributes){
                        //"tagName:" + childElement.getName()
                        multiTableQuerySQL.put(attribute.getValue(), childElement.getTextTrim());
                    }
                }
            }
        }
    }

    private File getResourcesFile(){
        File rootFile = new File(this.getClass().getResource("/").getPath()).getParentFile();
        File[] files = rootFile.listFiles();
        for(File file : files){
            if(file.getName().contains("resources")){
                return file;
            }
        }
        return null;
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