package com.amarhu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AmarhuBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmarhuBackendApplication.class, args);
	}

}

