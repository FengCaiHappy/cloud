package com.feng.consumer.controller;

import com.feng.consumer.feign.FeignDemo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class DemoController {

    @Autowired
    FeignDemo feignDemo;

    @RequestMapping("/demo")
    @ResponseBody
    public String demo(){
        return feignDemo.demo();
    }
}
