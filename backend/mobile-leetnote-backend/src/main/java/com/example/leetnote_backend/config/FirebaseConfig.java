package com.example.leetnote_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {

        InputStream firebaseConfigStream = this.getClass().getClassLoader().getResourceAsStream("leetnote-app-7f481-firebase-adminsdk.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(firebaseConfigStream))
                .build();

        return FirebaseApp.initializeApp(options);
    }
}
