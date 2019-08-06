package com.feng.autoinjection.core.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuickList<T extends QuickListInterface> {

    private List<T> list = new ArrayList<>();

    private Map<String, Integer> indexMap = new HashMap<>();

    public void addInfo(T info){
        this.list.add(info);
    }

    public List<T> getList(){
        return list;
    }

    public T getBean(String tagStr){
        if(indexMap.get(tagStr) != null){
            return list.get(indexMap.get(tagStr));
        } else {
          for(int i = 0, len = list.size(); i < len; i++){
              if(tagStr.equals(list.get(i).getTag())){
                  indexMap.put(tagStr, i);
                  return list.get(i);
              }
          }
        }
        return null;
    }
}
