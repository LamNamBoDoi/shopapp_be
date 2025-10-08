package com.example.shopapp.services.Payment;

import com.example.shopapp.enums.PaymentStatus;
import com.example.shopapp.models.Order;
import com.example.shopapp.models.Payment;
import com.example.shopapp.repositories.OrderRepository;
import com.example.shopapp.repositories.PaymentRepository;
import com.example.shopapp.services.Payment.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CODService implements PaymentStrategy {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Override
    public String getPaymentMethod() {
        return "COD";
    }

    @Override
    public String createPayment(Long amount, String orderInfo, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setOrderInfo(orderInfo);
        payment.setPaymentMethod("COD");
        payment.setStatus(PaymentStatus.pending);
        paymentRepository.save(payment);

        return "COD_PAYMENT_CREATED";
    }

    public Payment confirmCODPayment(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus(PaymentStatus.success);
        return paymentRepository.save(payment);
    }
}