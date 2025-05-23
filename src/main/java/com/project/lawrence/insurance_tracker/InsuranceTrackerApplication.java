package com.project.lawrence.insurance_tracker;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "")
public class InsuranceTrackerApplication {

	static {
		Dotenv dotenv = Dotenv.load();
		System.setProperty("SPRING_USERNAME", dotenv.get("SPRING_USERNAME"));
		System.setProperty("SPRING_PASSWORD", dotenv.get("SPRING_PASSWORD"));
		System.setProperty("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID"));
		System.setProperty("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET"));
		System.setProperty("MAIL_USERNAME", dotenv.get("MAIL_USERNAME"));
		System.setProperty("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD"));
	}

	public static void main(String[] args) {
		SpringApplication.run(InsuranceTrackerApplication.class, args);
	}

}
