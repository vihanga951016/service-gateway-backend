package com.flex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
		scanBasePackages = {
				"com.flex.*",
				"com.flex.common_module",
				"com.flex.service_module",
				"com.flex.user_module",
				"com.flex.job_module"
		}
)
@EnableCaching
@EnableScheduling
public class ServiceGatewayBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceGatewayBackendApplication.class, args);
	}

}
