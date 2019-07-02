package com.feng.consumer.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "eureka-client1")
public interface FeignDemo {

    @RequestMapping(value = "/demo")
    String demo();

}
