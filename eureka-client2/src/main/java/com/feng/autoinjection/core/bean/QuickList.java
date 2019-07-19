package com.feng.autoinjection.core.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuickList {

    private List<TableMapperInfo> mapperList = new ArrayList<>();

    private Map<String, Integer> indexMap = new HashMap<>();

    public void addInfo(TableMapperInfo info){
        this.mapperList.add(info);
    }

    public List<TableMapperInfo> getMapperList(){
        return mapperList;
    }

    public TableMapperInfo getBean(String tableName){
        if(indexMap.get(tableName) != null){
            return mapperList.get(indexMap.get(tableName));
        } else {
          for(int i = 0 , len = mapperList.size(); i < len; i++){
              if(tableName.equals(mapperList.get(i).getTableName())){
                  indexMap.put(tableName, i);
                  return mapperList.get(i);
              }
          }
        }
        return null;
    }
}
