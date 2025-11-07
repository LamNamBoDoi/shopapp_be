package com.example.shopapp.controller;

import com.example.shopapp.components.LocalizationUtils;
import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.dtos.OrderDetailDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.OrderDetail;
import com.example.shopapp.response.*;
import com.example.shopapp.services.OrderDetailService.IOrderDetailService;
import com.example.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/order_details")
public class OrderDetailController extends TranslateMessages {
    private final IOrderDetailService orderDetailService;
    private final LocalizationUtils localizationUtils;

    //Thêm mới 1 order detail
    @PostMapping("")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<?>> createOrderDetail(
            @Valid @RequestBody OrderDetailDTO orderDetailDTO
    ) {
        OrderDetail newOrderDetail = orderDetailService.createOrderDetail(orderDetailDTO);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(translate(MessageKeys.CREATE_ORDER_DETAILS_SUCCESS))
                .payload(OrderDetailResponse.fromOrderDetail(newOrderDetail))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<ApiResponse<?>> getOrderDetail(@Valid @PathVariable Long id) {
        OrderDetail orderDetail = orderDetailService.getOrderDetail(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(translate(MessageKeys.GET_INFORMATION_SUCCESS))
                .payload(OrderDetailResponse.fromOrderDetail(orderDetail))
                .build());
    }

    //Lấy ra danh sách các order_details của 1 order nào đó
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<ApiResponse<?>> getOrderDetails(@Valid @PathVariable Long orderId) {
        List<OrderDetail> orderDetails = orderDetailService.findByOrderId(orderId);
        List<OrderDetailResponse> orderDetailResponses =
                orderDetails.stream().map(OrderDetailResponse::fromOrderDetail).toList();
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(translate(MessageKeys.GET_INFORMATION_SUCCESS))
                .payload(orderDetailResponses)
                .build());
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<ApiResponse<?>> getProductPurchased(@PathVariable Long userId) {
        List<ProductResponse> productDetails = orderDetailService.getProductsPurchasedByUserID(userId)
                .stream()
                .map(ProductResponse::fromProduct)
                .toList();
        return ResponseEntity.ok(ApiResponse.<List<ProductResponse>>builder()
                .success(true)
                .message(translate(MessageKeys.GET_INFORMATION_SUCCESS))
                .payload(productDetails)
                .build());
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> updateOrderDetail(
            @Valid @PathVariable Long id,
            @RequestBody OrderDetailDTO orderDetailData
    ) throws DataNotFoundException {
        OrderDetail orderDetail = orderDetailService.updateOrderDetail(id, orderDetailData);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(translate(MessageKeys.UPDATE_ORDER_DETAILS_SUCCESS))
                .payload(OrderDetailResponse.fromOrderDetail(orderDetail))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteOrderDetail(@Valid @PathVariable Long id) {
        orderDetailService.deleteOrderDetail(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(localizationUtils.getLocalizedMessage(MessageKeys.MESSAGE_DELETE_SUCCESS, id))
                .payload(id)
                .build());
    }
}
