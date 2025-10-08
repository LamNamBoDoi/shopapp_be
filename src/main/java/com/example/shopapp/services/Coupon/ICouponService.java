package com.example.shopapp.services.Coupon;

public interface ICouponService {
    double calculateCouponValue(String couponCode, double totalAmount);
}
