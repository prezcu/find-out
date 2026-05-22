package dev.andrei.app_backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class AppBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppBackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner testDatabaseConnection(JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				// Runs a cheap query to force a round-trip to the DB
				Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
				System.out.println(">>> DATABASE CONNECTION SUCCESSFUL! Result: " + result);
			} catch (Exception e) {
				System.err.println(">>> DATABASE CONNECTION FAILED: " + e.getMessage());
			}
		};
	}

}
