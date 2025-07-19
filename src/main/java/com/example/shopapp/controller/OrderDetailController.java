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
    public ResponseEntity<?> createOrderDetail(
            @Valid @RequestBody OrderDetailDTO orderDetailDTO
            ){
        OrderDetail newOrderDetail =  orderDetailService.createOrderDetail(orderDetailDTO);
        return ResponseEntity.ok(OrderDetailResponse.fromOrderDetail(newOrderDetail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(
            @Valid @PathVariable Long id
    ){
        OrderDetail orderDetail = orderDetailService.getOrderDetail(id);
        return ResponseEntity.ok(OrderDetailResponse.fromOrderDetail(orderDetail));
    }

    //Lấy ra danh sách các order_details của 1 order nào đó
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderDetails(
            @Valid @PathVariable Long orderId
    ){
        List<OrderDetail> orderDetails = orderDetailService.findByOrderId(orderId);
        List<OrderDetailResponse> orderDetailResponses = orderDetails.stream().map(OrderDetailResponse::fromOrderDetail).toList();
        return ResponseEntity.ok(orderDetailResponses);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<?>> getProductPurchased(@PathVariable Long userId) {
        try {
            List<ProductResponse> productDetails = orderDetailService.getProductsPurchasedByUserID(userId).stream().map(
                    (ProductResponse::fromProduct)
            ).toList();
            return ResponseEntity.ok(ApiResponse.<List<ProductResponse>>builder()
                    .success(true)
                    .message(translate(MessageKeys.GET_INFORMATION_SUCCESS))
                    .payload(productDetails).build());
        }catch (Exception e){
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .message(translate(MessageKeys.MESSAGE_ERROR_GET)).error(e.getMessage()).build()
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderDetail(
            @Valid @PathVariable Long id,
            @RequestBody OrderDetailDTO orderDetailData
    ){
        try {
            OrderDetail orderDetail = orderDetailService.updateOrderDetail(id, orderDetailData);
            return ResponseEntity.ok(orderDetail);

        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrderDetail(
            @Valid @PathVariable Long id
    ){
        orderDetailService.deleteOrderDetail(id);
        return ResponseEntity.ok().body(localizationUtils.getLocalizedMessage(MessageKeys.CREATE_ORDER_DETAILS_SUCCESS));
    }
}
