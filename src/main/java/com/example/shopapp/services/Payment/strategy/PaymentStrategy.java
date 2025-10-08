package com.example.shopapp.services.Payment.strategy;

public interface PaymentStrategy {
    String createPayment(Long amount, String orderInfo, Long orderId);
    String getPaymentMethod();
}
