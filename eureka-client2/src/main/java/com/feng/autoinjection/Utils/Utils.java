package com.feng.autoinjection.Utils;

import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.*;

public class Utils {

    private static Map<String, String> cacheMap;

    public static List<String> getKeyFromMap(Map<String, Object> map){
        List<String> result = new ArrayList<>();
        Iterator<String> iter = map.keySet().iterator();
        while(iter.hasNext()){
            result.add(iter.next());
        }
        return result;
    }

    public static String getYMLProperties(String key){
        if(cacheMap != null){
            return cacheMap.get(key);
        }
        try {
            Yaml yaml = new Yaml();
            InputStream resourceAsStream = Utils.class.getClassLoader().getResourceAsStream("application.yml");
            cacheMap = (Map) yaml.load(resourceAsStream);
            return cacheMap.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
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
}
