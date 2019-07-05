package com.feng.common;

import java.lang.reflect.Method;

public class UserDaoProvider implements InterfaceProvider{
    @Override
    public boolean support(Object cls) {
        return IUserDao.class == cls;
    }

    @Override
    public Object implMethod(Method method, Object[] args) {
        return "method call success!";
    }
}
