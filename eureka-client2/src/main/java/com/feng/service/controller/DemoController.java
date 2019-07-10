package com.feng.service.controller;

import com.feng.entity.UserInfo;
import com.feng.service.dao.DemoDao;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Controller
public class DemoController {

    @Resource
    private DemoDao demoDao;

    @RequestMapping("/demo")
    @ResponseBody
    public String demo(){
        List<UserInfo> list = demoDao.list();
        return "demo2";
    }
}
