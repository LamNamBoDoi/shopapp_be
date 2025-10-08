package com.example.shopapp.services.Payment;

import com.example.shopapp.dtos.PaymentDTO;
import com.example.shopapp.enums.OrderStatus;
import com.example.shopapp.enums.PaymentStatus;
import com.example.shopapp.models.Order;
import com.example.shopapp.models.Payment;
import com.example.shopapp.repositories.OrderRepository;
import com.example.shopapp.repositories.PaymentRepository;
import com.example.shopapp.response.PaymentResponse;
import com.example.shopapp.services.Payment.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final List<PaymentStrategy> paymentStrategies;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public PaymentResponse processPayment(PaymentDTO paymentDTO) {
        log.info("Processing payment for order: {}, method: {}",
                paymentDTO.getOrderId(), paymentDTO.getPaymentMethod());

        // 1. Validate order exists
        Order order = orderRepository.findById(paymentDTO.getOrderId())
                .orElseThrow(() -> new RuntimeException(
                        "Order not found with id: " + paymentDTO.getOrderId()));

        // 2. Check if order already paid
        Payment existingPayment = paymentRepository.findByOrderId(paymentDTO.getOrderId())
                .orElse(null);

        // 3. Handle COD separately (no need payment URL)
        if ("COD".equalsIgnoreCase(paymentDTO.getPaymentMethod())) {
            return handleCODPayment(order, paymentDTO);
        }

        // 4. Find payment strategy for online payment
        PaymentStrategy strategy = paymentStrategies.stream()
                .filter(s -> s.getPaymentMethod().equalsIgnoreCase(paymentDTO.getPaymentMethod()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Payment method not supported: " + paymentDTO.getPaymentMethod()));

        // 5. Create payment URL
        String paymentUrl = strategy.createPayment(
                paymentDTO.getAmount(),
                paymentDTO.getOrderInfo(),
                paymentDTO.getOrderId()
        );

        // 6. Get created payment record
        Payment payment = paymentRepository.findByOrderId(paymentDTO.getOrderId())
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        log.info("Payment created successfully for order: {}", paymentDTO.getOrderId());

        // 7. Return response
        return PaymentResponse.fromPayment(payment, paymentUrl);
    }

    public Optional<Payment> findByOrderId(Long orderId){
        return paymentRepository.findByOrderId(orderId);
    }

    /**
     * Xử lý thanh toán COD - không cần URL
     */
    private PaymentResponse handleCODPayment(Order order, PaymentDTO paymentDTO) {
        // Tạo hoặc update payment record
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElse(Payment.builder()
                        .order(order)
                        .build());

        payment.setPaymentMethod("COD");
        payment.setAmount(paymentDTO.getAmount());
        payment.setOrderInfo(paymentDTO.getOrderInfo());
        payment.setStatus(PaymentStatus.pending); // COD sẽ update khi nhận hàng
        payment.setTransactionNo("COD_" + order.getId() + "_" + System.currentTimeMillis());

        payment = paymentRepository.save(payment);

        // Update order status
        order.setStatus(OrderStatus.pending);
        orderRepository.save(order);

        return PaymentResponse.fromPayment(payment, null);
    }
}