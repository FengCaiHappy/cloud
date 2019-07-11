package com.feng.autoinjection.Utils;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
}
