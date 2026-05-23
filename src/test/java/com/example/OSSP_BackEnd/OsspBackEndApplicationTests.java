package com.example.OSSP_BackEnd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class OsspBackEndApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void encodePassword() {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode("password123!");
		System.out.println("Encoded Password: " + encodedPassword);
	}
}