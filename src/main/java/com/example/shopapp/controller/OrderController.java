package com.example.shopapp.controller;

import com.example.shopapp.components.LocalizationUtils;
import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.dtos.OrderDTO;
import com.example.shopapp.dtos.PaymentDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Order;
import com.example.shopapp.response.ApiResponse;
import com.example.shopapp.response.OrderPageResponse;
import com.example.shopapp.response.OrderResponse;
import com.example.shopapp.response.PaymentResponse;
import com.example.shopapp.services.Order.IOrderService;
import com.example.shopapp.services.Payment.PaymentService;
import com.example.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController extends TranslateMessages {
    private final IOrderService orderService;
    private final LocalizationUtils localizationUtils;
    private final PaymentService paymentService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("")
    public ResponseEntity<ApiResponse<?>> createOrder(
            @RequestBody @Valid OrderDTO orderDTO,
            BindingResult result
    ) throws DataNotFoundException {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message(translate(MessageKeys.ERROR_MESSAGE))
                    .error(String.join(", ", errorMessages))
                    .build());
        }

        // Tạo order
        OrderResponse orderResponse = orderService.createOrder(orderDTO);

        // Tạo payment DTO
        PaymentDTO paymentDTO = PaymentDTO.builder()
                .orderId(orderResponse.getId())
                .paymentMethod(orderDTO.getPaymentMethod())
                .amount(orderResponse.getTotalMoney().longValue())
                .orderInfo("Thanh toán đơn hàng #" + orderResponse.getId())
                .build();

        try {
            // Gọi payment service
            PaymentResponse paymentResponse = paymentService.processPayment(paymentDTO);

            // Build response payload
            Map<String, Object> responsePayload = new HashMap<>();
            responsePayload.put("order", orderResponse);
            responsePayload.put("payment", paymentResponse);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message(translate(MessageKeys.CREATE_ORDER_SUCCESS))
                    .payload(responsePayload)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Không thể tạo thanh toán: " + e.getMessage())
                    .error(e.getMessage())
                    .build());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllOrders() {
        List<Order> orders = orderService.findAllOrders(); // service trả về tất cả orders
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(translate(MessageKeys.GET_INFORMATION_SUCCESS))
                .payload(OrderResponse.fromOrdersList(orders))
                .build());
    }


    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @GetMapping("/user/{user_id}")
    public ResponseEntity<ApiResponse<?>> getOrders(@Valid @PathVariable("user_id") Long userId) {
        List<Order> orders = orderService.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(translate(MessageKeys.GET_INFORMATION_SUCCESS))
                .payload(OrderResponse.fromOrdersList(orders))
                .build());
    }


    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getOrder(@PathVariable("id") Long orderId) {
        Order existingOrder = orderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(translate(MessageKeys.GET_INFORMATION_SUCCESS))
                .payload(OrderResponse.fromOrder(existingOrder))
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateOrders(
            @PathVariable() Long id,
            @Valid @RequestBody OrderDTO orderDTO
    ) {
        Order order = orderService.updateOrder(id, orderDTO);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(translate(MessageKeys.UPDATE_ORDER_SUCCESS))
                .payload(OrderResponse.fromOrder(order))
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteOrders(
            @PathVariable() Long id
    ) {
        orderService.deleteOrder(id); // xóa mềm => cập nhật active = false
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(localizationUtils.getLocalizedMessage(MessageKeys.MESSAGE_DELETE_SUCCESS, id))
                .payload(id)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get-orders-by-keyword")
    public ResponseEntity<ApiResponse<?>> getOrderByKeyword(
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "10", name = "limit") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(
                page,
                limit,
                Sort.by("id").ascending()
        );
        Page<OrderResponse> orderPage = orderService.findByKeyword(keyword, pageRequest);
        OrderPageResponse pageResponse = OrderPageResponse.builder()
                .orders(orderPage.getContent())
                .pageNumber(page)
                .totalElements(orderPage.getTotalElements())
                .pageSize(orderPage.getSize())
                .isLast(orderPage.isLast())
                .totalPages(orderPage.getTotalPages())
                .build();

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(translate(MessageKeys.GET_INFORMATION_SUCCESS))
                .payload(pageResponse)
                .build());
    }
}
