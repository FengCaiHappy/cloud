package com.feng;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
@MapperScan("com.feng")
public class ServiceApplication2 {

	public static void main(String[] args) {
		SpringApplication.run(ServiceApplication2.class, args);
	}

}
