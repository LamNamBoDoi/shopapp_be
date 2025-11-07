package com.example.shopapp.enums;

public enum OrderStatus {
    pending,      // Đơn hàng mới, chưa xử lý
    processing,   // Đang xử lý
    shipped,      // Đã giao hàng
    delivered,    // Đã nhận hàng
    cancelled,    // Đã hủy
    failed        // Thanh toán hoặc xử lý thất bại
}


