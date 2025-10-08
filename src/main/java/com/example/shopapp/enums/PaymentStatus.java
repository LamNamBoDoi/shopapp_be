package com.example.shopapp.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PaymentStatus {
    pending,
    success,
    failed,
    refunded,
    cancelled
}
