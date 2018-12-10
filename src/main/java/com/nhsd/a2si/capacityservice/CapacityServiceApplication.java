package com.nhsd.a2si.capacityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication(scanBasePackages = {"com.nhsd.a2si"})
@EnableTransactionManagement
public class CapacityServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CapacityServiceApplication.class, args);
	}
}
