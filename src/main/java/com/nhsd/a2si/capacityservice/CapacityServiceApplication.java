package com.nhsd.a2si.capacityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication()
@EnableTransactionManagement
public class CapacityServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CapacityServiceApplication.class, args);
	}
}
