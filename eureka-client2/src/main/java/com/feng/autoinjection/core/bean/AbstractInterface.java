package com.feng.autoinjection.core.bean;

public abstract class AbstractInterface implements QuickListInterface{

    @Override
    public boolean hasBean(String str){
        return false;
    }
}
