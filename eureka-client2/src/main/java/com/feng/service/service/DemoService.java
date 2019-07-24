package com.feng.service.service;

import com.feng.autoinjection.autoannotation.FMethodHandler;
import com.feng.autoinjection.core.opratehandler.DefaultHandler;
import com.feng.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@FMethodHandler(tableName = "userInfo")
@Service
public class DemoService extends DefaultHandler<UserInfo> {

    @Autowired
    private UserDao userDao;


    public List<Map> list(){
        return userDao.list();
    }
    @Override
    public void prepareList(UserInfo obj) {
        System.out.println("哈哈哈哈哈哈哈");
    }

    @Override
    public <UserInfo> List<UserInfo> afterList(ArrayList<UserInfo> obj) {
        System.out.println("哈哈哈哈哈哈哈");
        return obj;
    }

    @Override
    public <UserInfo> UserInfo afterQueryById(UserInfo UserInfo) {
        return UserInfo;
    }
}
