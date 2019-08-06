package com.feng.autoinjection.core.bean;

import lombok.Data;

@Data
public class TableMapperInfo extends AbstractInterface{
    private String tableName;

    private String mapperBeaName;

    private String methodHandlerName;

    private String urlName;

    @Override
    public String getTag() {
        return this.tableName;
    }

    @Override
    public boolean hasBean(String str){
        return (this.tableName.equals(str) || this.urlName.equals(str));
    }
}
