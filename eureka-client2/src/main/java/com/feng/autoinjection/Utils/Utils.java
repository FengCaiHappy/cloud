package com.feng.autoinjection.Utils;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Utils {

    public static List<String> getKeyFromMap(Map<String, Object> map){
        List<String> result = new ArrayList<>();
        Iterator<String> iter = map.keySet().iterator();
        while(iter.hasNext()){
            result.add(iter.next());
        }
        return result;
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

    public static Map<String, Object> beanTOMap(Object bean){
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
}
