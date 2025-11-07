package com.example.shopapp.services.Payment;

import com.example.shopapp.configurations.ZaloPayConfig;
import com.example.shopapp.enums.PaymentStatus;
import com.example.shopapp.models.Order;
import com.example.shopapp.models.Payment;
import com.example.shopapp.repositories.OrderRepository;
import com.example.shopapp.repositories.PaymentRepository;
import com.example.shopapp.services.Payment.strategy.PaymentStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZaloPayService implements PaymentStrategy {
    private final ZaloPayConfig zaloPayConfig;
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderRepository orderRepository;

    @Override
    public String getPaymentMethod() {
        return "ZALOPAY";
    }

    @Override
    public String createPayment(Long amount, String orderInfo, Long orderId) {
        try {
            Order order1 = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Tạo app_trans_id unique
            String currentTime = String.valueOf(System.currentTimeMillis());
            String appTransId = getCurrentTimeString("yyMMdd") + "_" + currentTime;

            // Lưu payment vào DB
            Payment payment = Payment.builder()
                    .order(order1)
                    .amount(amount)
                    .orderInfo(orderInfo)
                    .paymentMethod("ZALOPAY")
                    .status(PaymentStatus.pending)
                    .transactionNo(appTransId)
                    .build();
            paymentRepository.save(payment);

            // Tạo embed_data
            Map<String, String> embedData = new HashMap<>();
            embedData.put("redirecturl", zaloPayConfig.getRedirectUrl());

            String embedDataJson = objectMapper.writeValueAsString(embedData);

            // Tạo item array
            Map<String, Object>[] items = new Map[]{
                    new HashMap<String, Object>() {{
                        put("itemid", orderId);
                        put("itemname", orderInfo);
                        put("itemprice", amount);
                        put("itemquantity", 1);
                    }}
            };
            String itemsJson = objectMapper.writeValueAsString(items);

            long appTime = System.currentTimeMillis();

            // Tạo data để tính MAC theo đúng format ZaloPay
            String data = zaloPayConfig.getAppId() + "|"
                    + appTransId + "|"
                    + "user_" + orderId + "|"
                    + amount + "|"
                    + appTime + "|"
                    + embedDataJson + "|"
                    + itemsJson;

            String mac = hmacSHA256(zaloPayConfig.getKey1(), data);

            log.info("ZaloPay Data: {}", data);
            log.info("ZaloPay MAC: {}", mac);

            // Tạo order request
            Map<String, Object> order = new HashMap<>();
            order.put("app_id", Integer.parseInt(zaloPayConfig.getAppId()));
            order.put("app_trans_id", appTransId);
            order.put("app_user", "user_" + orderId);
            order.put("app_time", appTime);
            order.put("amount", amount);
            order.put("description", orderInfo);
            order.put("bank_code", "");
            order.put("item", itemsJson);
            order.put("embed_data", embedDataJson);
            order.put("callback_url", zaloPayConfig.getCallbackUrl());
            order.put("mac", mac);

            log.info("ZaloPay Order Request: {}", order);

            // Gọi API ZaloPay
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(order, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    zaloPayConfig.getEndpoint() + "/create",
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            log.info("ZaloPay Response: {}", responseBody);

            if (responseBody != null) {
                Integer returnCode = (Integer) responseBody.get("return_code");

                if (returnCode != null && returnCode == 1) {
                    String orderUrl = (String) responseBody.get("order_url");
                    if (orderUrl != null) {
                        return orderUrl;
                    }
                }

                String returnMessage = (String) responseBody.get("return_message");
                Integer subReturnCode = (Integer) responseBody.get("sub_return_code");
                String subReturnMessage = (String) responseBody.get("sub_return_message");

                log.error("ZaloPay Error - Code: {}, Message: {}, SubCode: {}, SubMessage: {}",
                        returnCode, returnMessage, subReturnCode, subReturnMessage);

                throw new RuntimeException("ZaloPay Error: " + returnMessage +
                        (subReturnMessage != null ? " - " + subReturnMessage : ""));
            }

            throw new RuntimeException("ZaloPay response body is null");

        } catch (Exception e) {
            log.error("Error creating ZaloPay payment: ", e);
            throw new RuntimeException("Error creating ZaloPay payment: " + e.getMessage(), e);
        }
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac hmac256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
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

    private String getCurrentTimeString(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }


}
