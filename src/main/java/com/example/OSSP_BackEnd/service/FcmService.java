package com.example.OSSP_BackEnd.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class FcmService {

    /**
     * 특정 유저의 기기 토큰으로 푸시 알림을 쏘는 메서드
     * * @param targetToken 알림을 받을 유저의 기기 토큰 (프론트가 주는 엄청 긴 문자열)
     * @param title       알림 제목 (예: "대여 매칭 완료!")
     * @param body        알림 내용 (예: "강현님이 대여 요청을 수락했습니다.")
     */
    public void sendMessageTo(String targetToken, String title, String body) {
        try {
            // 1. 알림(Notification) 생성
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // 2. 메시지(Message) 조립 (어떤 토큰에 어떤 알림을 보낼지)
            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(notification)
                    .build();

            // 3. 구글(FCM)로 전송!
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("🔔 알림 전송 성공! 메시지 ID: " + response);

        } catch (Exception e) {
            System.out.println("❌ 알림 전송 실패: " + e.getMessage());
        }
    }
}