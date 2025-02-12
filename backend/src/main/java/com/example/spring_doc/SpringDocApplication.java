package com.example.spring_doc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SpringDocApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringDocApplication.class, args);
	}

}
