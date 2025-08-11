package com.ricky.ricky_rest_api.service;
import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

@Service
public class FcmService {

	public void sendNotification(String token, String title, String body) {
		Message message = Message.builder()
				.setToken(token)
				.setNotification(Notification.builder()
						.setTitle(title)
						.setBody(body)
						.build())
				.build();

		try {
			String response = FirebaseMessaging.getInstance().send(message);
			System.out.println("FCM Message sent: " + response);
		} catch (FirebaseMessagingException e) {
			System.err.println("FCM Failed: " + e.getMessage());
		}
	}
}