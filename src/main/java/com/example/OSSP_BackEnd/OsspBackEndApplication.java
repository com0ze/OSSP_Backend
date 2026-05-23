package com.example.OSSP_BackEnd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class OsspBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(OsspBackEndApplication.class, args);
	}

}