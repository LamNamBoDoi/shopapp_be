package com.example.shopapp.services.Payment;

import com.example.shopapp.enums.OrderStatus;
import com.example.shopapp.enums.PaymentStatus;
import com.example.shopapp.models.Order;
import com.example.shopapp.models.Payment;
import com.example.shopapp.repositories.OrderRepository;
import com.example.shopapp.repositories.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCallbackService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void handlePaymentSuccess(Long orderId, String transactionId,
                                     String paymentMethod, String responseCode,
                                     Map<String, String> responseData) {
        log.info("Processing payment success for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setStatus(OrderStatus.pending);
        orderRepository.save(order);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));

        payment.setStatus(PaymentStatus.success);
        payment.setResponseCode(responseCode);

        // Extract bank code if exists
        if (responseData.containsKey("vnp_BankCode")) {
            payment.setBankCode(responseData.get("vnp_BankCode"));
        } else if (responseData.containsKey("bankCode")) {
            payment.setBankCode(responseData.get("bankCode"));
        }

        paymentRepository.save(payment);

        log.info("Payment success processed for order: {}, transaction: {}", orderId, transactionId);
    }

    @Transactional
    public void handlePaymentFailure(Long orderId, String reason,
                                     String responseCode, Map<String, String> responseData) {
        log.warn("Processing payment failure for order: {}, reason: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setStatus(OrderStatus.failed);
        orderRepository.save(order);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));

        payment.setStatus(PaymentStatus.failed);
        payment.setResponseCode(responseCode);
        paymentRepository.save(payment);

        log.info("Payment failure processed for order: {}", orderId);
    }

    @Transactional
    public void handleZaloPayCallback(Map<String, Object> callbackData, String key2) {
        try {
            String dataStr = (String) callbackData.get("data");
            String mac = (String) callbackData.get("mac");

            // Verify MAC
            String calculatedMac = hmacSHA256(key2, dataStr);
            if (!mac.equals(calculatedMac)) {
                throw new RuntimeException("Invalid MAC signature");
            }

            // Parse data
            Map<String, Object> data = objectMapper.readValue(dataStr, Map.class);

            String appTransId = (String) data.get("app_trans_id");
            Long amount = ((Number) data.get("amount")).longValue();

            // Find payment by transaction no
            Payment payment = paymentRepository.findByTransactionNo(appTransId)
                    .orElseThrow(() -> new RuntimeException("Payment not found: " + appTransId));

            // Update status
            handlePaymentSuccess(
                    payment.getOrder().getId(),
                    appTransId,
                    "ZALOPAY",
                    "1",
                    objectMapper.convertValue(data, Map.class)
            );

        } catch (Exception e) {
            log.error("Error processing ZaloPay callback", e);
            throw new RuntimeException("Error processing ZaloPay callback", e);
        }
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac hmac256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac256.init(secretKey);
            byte[] result = hmac256.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA256", e);
        }
    }
}
