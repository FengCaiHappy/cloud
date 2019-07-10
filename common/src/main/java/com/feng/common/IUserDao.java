package com.feng.common;

import org.springframework.web.bind.annotation.ResponseBody;

public interface IUserDao {

    @ResponseBody
    public String getUserName();
}
