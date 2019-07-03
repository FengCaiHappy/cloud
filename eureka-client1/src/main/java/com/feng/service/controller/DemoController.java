package com.feng.service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class DemoController {

    @Value("${config.value}")
    private String value;

    @RequestMapping("/demo")
    @ResponseBody
    public String demo(){
        return value;
    }
}
