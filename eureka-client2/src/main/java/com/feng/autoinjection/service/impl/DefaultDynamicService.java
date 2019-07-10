package com.feng.autoinjection.service.impl;

import com.feng.autoinjection.daoexecutor.IDaoExecutor;
import com.feng.autoinjection.service.IDynamicService;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultDynamicService implements IDynamicService {

    @Autowired
    private IDaoExecutor daoExecutor;

    public String index(Object param, String tableName) {
        return tableName + "Index";
    }

    public Object queryById(Object param, String tableName) {
        return null;
    }

    public Object list(Object param, String tableName) {
        return daoExecutor.list(param, tableName);
    }

    public Object update(Object param, String tableName) {
        return "123";
    }

    public Object delete(Object param, String tableName) {
        return null;
    }

    public Object add(Object param, String tableName) {
        return null;
    }

}
