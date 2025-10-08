package com.example.shopapp.services.Payment;

import com.example.shopapp.configurations.MoMoConfig;
import com.example.shopapp.enums.PaymentStatus;
import com.example.shopapp.models.Order;
import com.example.shopapp.models.Payment;
import com.example.shopapp.repositories.OrderRepository;
import com.example.shopapp.repositories.PaymentRepository;
import com.example.shopapp.services.Payment.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoMoService implements PaymentStrategy {

    private final MoMoConfig moMoConfig;
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final OrderRepository orderRepository;

    @Override
    public String getPaymentMethod() {
        return "MOMO";
    }

    @Override
    public String createPayment(Long amount, String orderInfo, Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(amount);
            payment.setOrderInfo(orderInfo);
            payment.setPaymentMethod("MOMO");
            payment.setStatus(PaymentStatus.pending);
            paymentRepository.save(payment);

            String requestId = String.valueOf(System.currentTimeMillis());
            String extraData = "";

            String rawSignature = "accessKey=" + moMoConfig.getAccessKey()
                    + "&amount=" + amount
                    + "&extraData=" + extraData
                    + "&ipnUrl=" + moMoConfig.getNotifyUrl()
                    + "&orderId=" + orderId
                    + "&orderInfo=" + orderInfo
                    + "&partnerCode=" + moMoConfig.getPartnerCode()
                    + "&redirectUrl=" + moMoConfig.getReturnUrl()
                    + "&requestId=" + requestId
                    + "&requestType=captureWallet";

            String signature = hmacSHA256(moMoConfig.getSecretKey(), rawSignature);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", moMoConfig.getPartnerCode());
            requestBody.put("partnerName", "Test");
            requestBody.put("storeId", "MomoTestStore");
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", moMoConfig.getReturnUrl());
            requestBody.put("ipnUrl", moMoConfig.getNotifyUrl());
            requestBody.put("lang", "vi");
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", "captureWallet");
            requestBody.put("signature", signature);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    moMoConfig.getEndpoint(), entity, Map.class);

            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null) {
                Object resultCode = responseBody.get("resultCode");

                if (resultCode != null && !"0".equals(String.valueOf(resultCode))) {
                    String message = (String) responseBody.get("message");
                    throw new RuntimeException("MoMo Error: " + message);
                }

                Object payUrl = responseBody.get("payUrl");
                if (payUrl != null) {
                    return payUrl.toString();
                }

                throw new RuntimeException("MoMo không trả về payUrl");
            }

            throw new RuntimeException("MoMo response body is null");

        } catch (Exception e) {
            log.error("Error creating MoMo payment: ", e);
            throw new RuntimeException("Error creating MoMo payment: " + e.getMessage(), e);
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
}