package com.example.OSSP_BackEnd.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {

    /**
     * 특정 유저의 기기 토큰으로 푸시 알림을 쏘는 메서드
     * @param targetToken 알림을 받을 유저의 기기 토큰 (프론트가 주는 엄청 긴 문자열)
     * @param title       알림 제목 (예: "대여 매칭 완료!")
     * @param body        알림 내용 (예: "강현님이 대여 요청을 수락했습니다.")
     */
    public void sendMessageTo(String targetToken, String title, String body) {
        try {
            // 1. 알림(Notification) 조립
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // 2. 메시지(Message) 객체 생성 및 OS별 최고 우선순위(Priority) 강제 주입
            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(notification)
                    
                    // 🔥 [추가됨] 안드로이드(Android) 잠자기 모드 무시하고 즉시 알림
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    
                    // 🔥 [추가됨] 아이폰(iOS) 즉시 알림 및 기본 알림음 소리 켜기
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("default")
                                    .build())
                            .putHeader("apns-priority", "10")
                            .build())
                    .build();

            // 3. 구글(FCM) 서버로 전송
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("🔔 알림 전송 성공! 메시지 ID: {}", response);

        } catch (Exception e) {
            log.error("❌ 알림 전송 실패: {}", e.getMessage());
        }
    }
}