package com.example.shopapp.response;

import com.example.shopapp.enums.PaymentStatus;
import com.example.shopapp.models.Payment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    @JsonProperty("payment_id")
    private Long paymentId;

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("amount")
    private Long amount;

    @JsonProperty("status")
    private PaymentStatus status; // PENDING, SUCCESS, FAILED

    @JsonProperty("payment_url")
    private String paymentUrl;

    @JsonProperty("transaction_no")
    private String transactionNo;

    @JsonProperty("order_info")
    private String orderInfo;

    @JsonProperty("bank_code")
    private String bankCode;

    @JsonProperty("response_code")
    private String responseCode;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    // Static method để convert từ Payment entity
    public static PaymentResponse fromPayment(Payment payment, String paymentUrl) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrder().getId())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentUrl(paymentUrl)
                .transactionNo(payment.getTransactionNo())
                .orderInfo(payment.getOrderInfo())
                .bankCode(payment.getBankCode())
                .responseCode(payment.getResponseCode())
                .createdAt(payment.getCreatedAt() != null
                        ? payment.getCreatedAt().format(formatter) : null)
                .updatedAt(payment.getUpdatedAt() != null
                        ? payment.getUpdatedAt().format(formatter) : null)
                .build();
    }
}
