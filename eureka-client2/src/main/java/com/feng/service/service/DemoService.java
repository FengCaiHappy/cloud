package com.feng.service.service;

import com.feng.autoinjection.autoannotation.FMethodHandler;
import com.feng.autoinjection.core.opratehandler.DefaultHandler;
import com.feng.entity.LogFileName;
import com.feng.entity.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;


@FMethodHandler(tableName = "userInfo")
@Service
public class DemoService extends DefaultHandler<UserInfo> {
    Logger XJK_USER_LOG = LoggerFactory.getLogger(DemoService.class);
    Logger BAITIAO_USER_LOG = LoggerUtils.Logger(LogFileName.BAITIAO_USER);


    @Override
    public void prepareList(UserInfo obj) {

        XJK_USER_LOG.info("小金库用户进来了...");
        BAITIAO_USER_LOG.info("白条用户进来了...");
        System.out.println("哈哈哈哈哈哈哈");
        try{
            throw new NullPointerException("abcd");
        }catch (Exception e){
            XJK_USER_LOG.error("",e);
        }

    }

    @Override
    public Object afterList(ArrayList<UserInfo> obj) {
        System.out.println("哈哈哈哈哈哈哈");
        return obj;
    }

    @Override
    public void prepareQueryById(UserInfo obj){
        System.out.println("哈哈哈哈哈哈哈");
    }

    @Override
    public Object afterQueryById(UserInfo UserInfo) {
        return UserInfo;
    }

    @Override
    public Object afterAdd(Integer obj){
        return obj;
    }
}
