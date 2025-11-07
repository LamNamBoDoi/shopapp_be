package com.example.shopapp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
    @JsonProperty("order_id")
    @NotNull(message = "Order ID is required")
    @Min(value = 1, message = "Order ID must be greater than 0")
    private Long orderId;

    @JsonProperty("payment_method")
    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "^(COD|VNPAY|MOMO|ZALOPAY)$",
            message = "Payment method must be: COD, VNPAY, MOMO, or ZALOPAY")
    private String paymentMethod;

    @JsonProperty("amount")
    @NotNull(message = "Amount is required")
    @Min(value = 1000, message = "Amount must be at least 1000")
    private Long amount;

    @JsonProperty("order_info")
    @NotBlank(message = "Order info is required")
    @Size(max = 500, message = "Order info must not exceed 500 characters")
    private String orderInfo;

    @JsonProperty("bank_code")
    private String bankCode; // Optional - cho VNPay, MoMo

}
