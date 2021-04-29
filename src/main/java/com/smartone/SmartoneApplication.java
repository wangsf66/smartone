package com.smartone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

import com.douglei.orm.spring.boot.starter.TransactionComponentScan;
import com.smartone.ddm.util.MappingContextRegisterListener;

@EnableEurekaClient
@SpringBootApplication
@ComponentScan(basePackages= {"com.ibs","com.smartone"})
@TransactionComponentScan(packages = { "com.ibs","com.smartone" })
public class SmartoneApplication {
	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(SmartoneApplication.class);
		springApplication.addListeners(new MappingContextRegisterListener());
		springApplication.run(args);
	}
}
