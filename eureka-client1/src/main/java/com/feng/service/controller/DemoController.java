package com.feng.service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class DemoController {

    @RequestMapping("/demo")
    @ResponseBody
    public String demo(){
        return "demo";
    }
}
