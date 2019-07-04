package com.feng.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
@EnableEurekaServer
public class CloudApplication2 {

	public static void main(String[] args) {
		SpringApplication.run(CloudApplication2.class, args);
	}

}
