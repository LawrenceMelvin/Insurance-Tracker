package com.project.lawrence.insurance_tracker;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "")
public class InsuranceTrackerApplication {

	static {
		Dotenv dotenv = Dotenv.load();
		System.setProperty("SPRING_USERNAME", dotenv.get("SPRING_USERNAME"));
		System.setProperty("SPRING_PASSWORD", dotenv.get("SPRING_PASSWORD"));
	}

	public static void main(String[] args) {
		SpringApplication.run(InsuranceTrackerApplication.class, args);
	}

}
