package com.ricky.ricky_rest_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
public class Main {

	@PostConstruct
	public void initializeFirebase() throws IOException {
		ClassPathResource resource = new ClassPathResource("service-account-key.json");
		try (InputStream serviceAccount = resource.getInputStream()) {
			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.build();

			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(options);
				System.out.println("Firebase Admin SDK initialized");
			}
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

}
