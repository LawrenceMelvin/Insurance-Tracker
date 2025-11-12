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
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().ignoreIfMalformed().load();
		// Use the helper method for all properties
		setSystemPropertyIfPresent("SPRING_USERNAME", dotenv);
		setSystemPropertyIfPresent("SPRING_PASSWORD", dotenv);
		setSystemPropertyIfPresent("GOOGLE_CLIENT_ID", dotenv);
		setSystemPropertyIfPresent("GOOGLE_CLIENT_SECRET", dotenv);
		setSystemPropertyIfPresent("MAIL_HOST", dotenv);
		setSystemPropertyIfPresent("MAIL_PORT", dotenv);
		setSystemPropertyIfPresent("MAIL_USERNAME", dotenv);
		setSystemPropertyIfPresent("MAIL_PASSWORD", dotenv);
		setSystemPropertyIfPresent("DATASOURCE_URL", dotenv);
		setSystemPropertyIfPresent("DATASOURCE_USERNAME", dotenv);
		setSystemPropertyIfPresent("DATASOURCE_PASSWORD", dotenv);
		setSystemPropertyIfPresent("FRONTEND_URL", dotenv);
		setSystemPropertyIfPresent("OPENAI_API_KEY", dotenv);
	}

	private static void setSystemPropertyIfPresent(String key, Dotenv dotenv) {
		String value = dotenv.get(key);
		if (value == null) {
			value = System.getenv(key); // fallback to system env variables
		}
		if (value != null) {
			System.setProperty(key, value);
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(InsuranceTrackerApplication.class, args);
	}

}
