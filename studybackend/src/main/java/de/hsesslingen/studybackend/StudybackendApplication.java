package de.hsesslingen.studybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class StudybackendApplication {

	public static void main(String[] args) {
		// Load environment variables from .env
        Dotenv dotenv = Dotenv.load();

        // Set them as system properties so Spring Boot can access them
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		
		SpringApplication.run(StudybackendApplication.class, args);
	}

}
