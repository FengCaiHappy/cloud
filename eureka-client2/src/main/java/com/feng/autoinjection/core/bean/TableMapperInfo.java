package com.feng.autoinjection.core.bean;

import lombok.Data;

@Data
public class TableMapperInfo implements QuickListInterface{
    private String tableName;

    private String mapperBeaName;

    private String methodHandlerName;

    @Override
    public String getTag() {
        return this.tableName;
    }
}
