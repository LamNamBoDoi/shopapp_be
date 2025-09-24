package com.example.shopapp.configurations;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {
    @PostConstruct
    // postconstruct là method sẽ được chạy ngay sau khi spring khởi động
    public void initialize(){
        try{
            InputStream serviceAccount = getClass().getResourceAsStream("/groceryshop-5a917-firebase-adminsdk-fbsvc-70f20e820c.json");

            assert serviceAccount != null;
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        return FirebaseMessaging.getInstance(FirebaseApp.getInstance());
    }
}
