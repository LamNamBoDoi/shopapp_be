package com.example.shopapp.models;

import com.example.shopapp.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payments")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private Long amount;

    @Column(name = "order_info", length = 500) // sửa từ 255 -> 500
    private String orderInfo;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "transaction_no", length = 100)
    private String transactionNo;

    @Column(name = "response_code", length = 10) // sửa từ 20 -> 10
    private String responseCode;

    @Column(name = "bank_code", length = 50)
    private String bankCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private PaymentStatus status;
}


