package com.feng.autoinjection.Utils;

import com.feng.autoinjection.autoannotation.FMethodHandler;
import com.feng.autoinjection.autoannotation.FTableName;
import com.feng.autoinjection.config.AutoAnnotationScanner;
import com.feng.autoinjection.core.bean.QuickList;
import com.feng.autoinjection.core.bean.TableMapperInfo;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Utils {

    public static List<String> getKeyFromMap(QuickList<TableMapperInfo> list){
        List<String> result = new ArrayList<>();
        Iterator<TableMapperInfo> iter = list.getList().listIterator();
        while(iter.hasNext()){
            TableMapperInfo tableMapperInfo = iter.next();
            result.add(StringUtils.isEmpty(tableMapperInfo.getUrlName())?
                    tableMapperInfo.getTableName():tableMapperInfo.getUrlName());
        }
        return result;
    }

    public static HttpServletRequest getHttpRequest(){
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return servletRequestAttributes.getRequest();
    }

    public static QuickList<TableMapperInfo> getBeanTableMapper(String basePackage){
        QuickList<TableMapperInfo> quickList = new QuickList();
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        Map<String, Object> tableNameBeans = getBeansWithAnnotation(basePackage, context, FTableName.class);
        Map<String, Object> fMethodHandlerBeans = getBeansWithAnnotation(basePackage, context, FMethodHandler.class);
        setMapperInfoList(1, tableNameBeans, FTableName.class, quickList);
        setMapperInfoList(2, fMethodHandlerBeans, FMethodHandler.class, quickList);
        return quickList;
    }

    private static void setMapperInfoList(int type, Map<String, Object> beans, Class<? extends Annotation> annotationClass, QuickList<TableMapperInfo> quickList){
        Iterator<String> iter = beans.keySet().iterator();
        while(iter.hasNext()) {
            Object value = beans.get(iter.next());
            if(type == 1){
                TableMapperInfo tableMapperInfo = new TableMapperInfo();
                setFtableNameInfo(tableMapperInfo, annotationClass, value, quickList);
            } else if(type == 2){
                setPrepareAndAfterInfo(annotationClass, value, quickList, "methodHandlerName");
            }
        }
    }

    private static Object getValueFromInvoke(Class<? extends Annotation> annotationClass, Object value, String key){
        Annotation annotation = value.getClass().getAnnotation(annotationClass);
        try {
            Method method = annotation.annotationType().getDeclaredMethod(key, null);
            if(!method.isAccessible()){
                method.setAccessible(true);
            }
            return method.invoke(annotation, null).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void setPrepareAndAfterInfo(Class<? extends Annotation> annotationClass, Object value, QuickList<TableMapperInfo> quickList, String filedName){
        try {
            String tableName = getValueFromInvoke(annotationClass, value, "tableName").toString();
            TableMapperInfo tableMapperInfo = quickList.getBean(tableName);
            if(tableMapperInfo != null){
                Field field = tableMapperInfo.getClass().getDeclaredField(filedName);
                field.setAccessible(true);
                String beanName = getValueFromInvoke(annotationClass, value, "beanName").toString();
                if(StringUtils.isEmpty(beanName)){
                    beanName = lowerFirst(value.getClass().getSimpleName());
                }
                field.set(tableMapperInfo, beanName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setFtableNameInfo(TableMapperInfo tableMapperInfo, Class<? extends Annotation> annotationClass, Object value, QuickList<TableMapperInfo> quickList){
        try {
            tableMapperInfo.setTableName(getValueFromInvoke(annotationClass, value, "tableName").toString());
            tableMapperInfo.setUrlName(getValueFromInvoke(annotationClass, value, "urlName").toString());
            tableMapperInfo.setMapperBeaName(value.getClass().getName());
            quickList.addInfo(tableMapperInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Object> getBeansWithAnnotation(String basePackage, GenericApplicationContext context, Class<? extends Annotation> type){
        AutoAnnotationScanner autoAnnotationScanner = new AutoAnnotationScanner(context, type);
        autoAnnotationScanner.registerTypeFilter();
        autoAnnotationScanner.scan(basePackage);
        return context.getBeansWithAnnotation(type);
    }



    public static Map<String, Object> getParameterMap(HttpServletRequest request) {
        Map<String, String[]> properties = request.getParameterMap();
        Map<String, Object> returnMap = new HashMap<>();
        Iterator<Map.Entry<String, String[]>> iter = properties.entrySet().iterator();
        String name, value = "";
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

    public static Map<String, Object> beanToMap(Object bean){
        Map<String,Object> map = new HashMap<>();
        BeanInfo info = null;
        try {
            info = Introspector.getBeanInfo(bean.getClass(), Object.class);
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        PropertyDescriptor[] pds = info.getPropertyDescriptors();
        for(PropertyDescriptor pd : pds) {
            String key = pd.getName();
            Object value = null;
            try {
                value = pd.getReadMethod().invoke(bean);
                if(StringUtils.isEmpty(value)){
                    continue;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            map.put(key, value);
        }
        return map;
    }

    public static Object mapToBean(Map map, Class type)
            throws IntrospectionException, IllegalAccessException,
            InstantiationException, InvocationTargetException {
        BeanInfo beanInfo = Introspector.getBeanInfo(type);
        Object obj = type.newInstance();

        PropertyDescriptor[] propertyDescriptors =  beanInfo.getPropertyDescriptors();
        for (int i = 0; i< propertyDescriptors.length; i++) {
            PropertyDescriptor descriptor = propertyDescriptors[i];
            String propertyName = descriptor.getName();
            if (map.containsKey(propertyName)) {
                Object value = map.get(propertyName);
                value = parseParameterValue(value, descriptor.getPropertyType());
                descriptor.getWriteMethod().invoke(obj, value);
            }
        }
        return obj;
    }

    private static Object parseParameterValue(Object value, Class clazz){
        if(clazz == Integer.class){
            return Integer.parseInt(value.toString());
        } else if(clazz == Float.class){
            return Float.parseFloat(value.toString());
        } else if(clazz == Date.class){
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            SimpleDateFormat dateFormat3 = new SimpleDateFormat("yyyy-MM-dd");
            try {
                value = dateFormat1.parse(value.toString());
            } catch (ParseException e) {
                try {
                    value = dateFormat2.parse(value.toString());
                } catch (ParseException e1) {
                    try {
                        value = dateFormat3.parse(value.toString());
                    } catch (ParseException e2) {
                        e2.printStackTrace();
                    }
                }
            }
            return value;
        } else {
            return value;
        }
    }


    public static String upperFirst(String oldStr) {
        char[] cs = oldStr.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);

    }

    public static String lowerFirst(String oldStr){

        char[]chars = oldStr.toCharArray();

        chars[0] += 32;

        return String.valueOf(chars);

    }
}
