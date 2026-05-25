package com.example.OSSP_BackEnd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling  // 스케줄러 활성화
@EnableAsync  // 비동기 처리 활성화
@SpringBootApplication
public class OsspBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(OsspBackEndApplication.class, args);
	}

}