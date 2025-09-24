package com.example.shopapp.services.Firebase;

import java.util.concurrent.ExecutionException;

public interface IFirebaseMessaginService {
    public String sendNotification(String title, String body, String token) throws InterruptedException, ExecutionException;
}
