package com.feng.autoinjection.controller;

import org.springframework.web.bind.annotation.ResponseBody;

public interface IDynamicUrlController<T> {

    String index();

    @ResponseBody
    <T> T add();

    @ResponseBody
    <T> T delete();

    @ResponseBody
    <T> T update();

    @ResponseBody
    <T> T list();

    @ResponseBody
    <T> T queryById();
}
