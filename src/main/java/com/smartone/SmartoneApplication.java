package com.smartone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

import com.smartone.ddm.util.MappingContextRegisterListener;

@EnableEurekaClient
@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@ComponentScan(basePackages= {"com.ibs","com.smartone"})
public class SmartoneApplication {
	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(SmartoneApplication.class);
		springApplication.addListeners(new MappingContextRegisterListener());
		springApplication.run(args);
	}
}
