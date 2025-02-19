package com.project.lawrence.insurance_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "")
public class InsuranceTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(InsuranceTrackerApplication.class, args);
	}

}
