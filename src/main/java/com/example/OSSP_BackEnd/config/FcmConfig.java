package com.example.OSSP_BackEnd.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FcmConfig {

    @PostConstruct
    public void init() {
        try {
            // resources 폴더에 넣은 json 열쇠 파일 이름
            InputStream serviceAccount = new ClassPathResource("firebase-service-key.json").getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // FirebaseApp이 이미 초기화되어 있지 않은 경우에만 초기화
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("🔥 Firebase 백엔드 연동 성공!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Firebase 연동 실패: " + e.getMessage());
        }
    }
}