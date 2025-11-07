package com.example.shopapp.controller;

import com.example.shopapp.dtos.PaymentDTO;
import com.example.shopapp.models.Payment;
import com.example.shopapp.response.ApiResponse;
import com.example.shopapp.response.PaymentResponse;
import com.example.shopapp.services.Payment.PaymentCallbackService;
import com.example.shopapp.services.Payment.PaymentService;
import com.example.shopapp.services.Payment.VNPayService;
import com.example.shopapp.configurations.ZaloPayConfig;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final VNPayService vnPayService;
    private final PaymentCallbackService callbackService;
    private final ZaloPayConfig zaloPayConfig;

    /**
     * Tạo payment - sử dụng PaymentDTO
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createPayment(
            @Valid @RequestBody PaymentDTO paymentDTO,
            BindingResult result) {

        // Validate
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Validation failed")
                    .error(String.join(", ", errorMessages))
                    .build());
        }

        try {
            PaymentResponse paymentResponse = paymentService.processPayment(paymentDTO);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message("Tạo thanh toán " + paymentDTO.getPaymentMethod() + " thành công")
                            .payload(paymentResponse)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Không thể tạo thanh toán")
                            .error(e.getMessage())
                            .build()
            );
        }
    }

    /**
     * VNPay return URL
     */
    @GetMapping("/vnpay-return")
    public ResponseEntity<ApiResponse<Map<String, Object>>> vnpayReturn(
            @RequestParam Map<String, String> params) {

        int result = vnPayService.handleReturn(params);
        Long orderId = Long.valueOf(params.get("vnp_TxnRef"));
        String transactionId = params.get("vnp_TransactionNo");
        String responseCode = params.get("vnp_ResponseCode");

        Map<String, Object> payload = new HashMap<>();
        String message;
        boolean success;

        if (result == 1) {
            success = true;
            message = "Thanh toán thành công";
            callbackService.handlePaymentSuccess(
                    orderId, transactionId, "VNPAY", responseCode, params
            );
        } else if (result == 0) {
            success = false;
            message = "Thanh toán thất bại";
            callbackService.handlePaymentFailure(
                    orderId, "Payment failed", responseCode, params
            );
        } else {
            success = false;
            message = "Chữ ký không hợp lệ";
            callbackService.handlePaymentFailure(
                    orderId, "Invalid signature", responseCode, params
            );
        }

        payload.put("orderId", orderId);
        payload.put("transactionId", transactionId);
        payload.put("success", success);

        return ResponseEntity.ok(
                ApiResponse.<Map<String, Object>>builder()
                        .success(success)
                        .message(message)
                        .payload(payload)
                        .build()
        );
    }

    /**
     * MoMo return URL
     */
    @GetMapping("/momo-return")
    public ResponseEntity<ApiResponse<Map<String, Object>>> momoReturn(
            @RequestParam Map<String, String> params) {

        String resultCode = params.get("resultCode");
        Long orderId = Long.valueOf(params.get("orderId"));
        String transId = params.get("transId");

        boolean success = "0".equals(resultCode);
        String message = success ? "Thanh toán thành công" : "Thanh toán thất bại";

        if (success) {
            callbackService.handlePaymentSuccess(
                    orderId, transId, "MOMO", resultCode, params
            );
        } else {
            callbackService.handlePaymentFailure(
                    orderId, "Payment failed", resultCode, params
            );
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", orderId);
        payload.put("transactionId", transId);
        payload.put("success", success);

        return ResponseEntity.ok(
                ApiResponse.<Map<String, Object>>builder()
                        .success(success)
                        .message(message)
                        .payload(payload)
                        .build()
        );
    }

    /**
     * MoMo IPN - server to server
     */
    @PostMapping("/momo-notify")
    public ResponseEntity<Map<String, Object>> momoNotify(
            @RequestBody Map<String, String> params) {

        String resultCode = params.get("resultCode");

        if (resultCode != null) {
            Long orderId = Long.valueOf(params.get("orderId"));
            String transId = params.get("transId");

            if ("0".equals(resultCode)) {
                callbackService.handlePaymentSuccess(
                        orderId, transId, "MOMO", resultCode, params
                );
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", 0);
        response.put("message", "success");

        return ResponseEntity.ok(response);
    }

    /**
     * ZaloPay callback - server to server
     */
    @PostMapping("/zalopay/callback")
    public ResponseEntity<Map<String, Object>> zalopayCallback(
            @RequestBody Map<String, Object> params) {

        try {
            callbackService.handleZaloPayCallback(params, zaloPayConfig.getKey2());

            Map<String, Object> response = new HashMap<>();
            response.put("return_code", 1);
            response.put("return_message", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("return_code", 0);
            response.put("return_message", e.getMessage());

            return ResponseEntity.ok(response);
        }
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<?>> getPaymentStatus(@PathVariable Long orderId) {
        try {
            var paymentOpt = paymentService.findByOrderId(orderId);
            if (paymentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .success(false)
                        .message("Không tìm thấy thanh toán cho đơn hàng #" + orderId)
                        .build());
            }

            var payment = paymentOpt.get();

            // Convert sang PaymentResponse (paymentUrl = null vì đây là truy vấn trạng thái)
            PaymentResponse paymentResponse = PaymentResponse.fromPayment(payment, null);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message("Trạng thái thanh toán của đơn hàng #" + orderId)
                            .payload(paymentResponse)
                            .build()
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy trạng thái thanh toán")
                    .error(e.getMessage())
                    .build());
        }
    }

}
