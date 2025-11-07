package com.example.shopapp.services.Firebase;

import org.springframework.stereotype.Service;
import com.google.firebase.messaging.*;
import java.util.concurrent.ExecutionException;

@Service
public class FirebaseMessagingService implements IFirebaseMessaginService{
    @Override
    public String sendNotification(String title, String body, String token) throws InterruptedException, ExecutionException {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .build();

        return FirebaseMessaging.getInstance().sendAsync(message).get();
    }
}
