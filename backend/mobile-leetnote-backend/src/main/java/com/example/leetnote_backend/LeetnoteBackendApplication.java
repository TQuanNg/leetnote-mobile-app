package com.example.leetnote_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LeetnoteBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(LeetnoteBackendApplication.class, args);
	}
}
