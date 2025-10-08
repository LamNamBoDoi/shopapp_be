package com.example.shopapp.services.Order;

import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.dtos.CartItemDTO;
import com.example.shopapp.dtos.OrderDTO;
import com.example.shopapp.enums.OrderStatus;
import com.example.shopapp.enums.PaymentStatus;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.*;
import com.example.shopapp.repositories.OrderDetailRepository;
import com.example.shopapp.repositories.OrderRepository;
import com.example.shopapp.repositories.ProductRepository;
import com.example.shopapp.repositories.UserRepository;
import com.example.shopapp.response.OrderResponse;
import com.example.shopapp.utils.MessageKeys;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService extends TranslateMessages implements IOrderService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    public Page<OrderResponse> findByKeyword(String keyword, Pageable pageable) {
        // lấy danh sách sản phẩm theo trang(page) và giới hạn(limit)
        Page<Order> orderPage;
        orderPage = orderRepository.findByKeyword(keyword, pageable);
        return orderPage.map(OrderResponse::fromOrder);
    }

    @Override
    @Transactional
    public OrderResponse createOrder(OrderDTO orderDTO) throws DataNotFoundException {
        // Tìm user
        User user = userRepository.findById(orderDTO.getUserId()).orElseThrow(
                () -> new DataNotFoundException("Can't find user with id: " + orderDTO.getUserId())
        );

        Order order = Order.builder()
                .user(user)
                .fullName(orderDTO.getFullName())
                .email(orderDTO.getEmail())
                .phoneNumber(orderDTO.getPhoneNumber())
                .address(orderDTO.getAddress())
                .note(orderDTO.getNote())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.pending)
                .totalMoney(orderDTO.getTotalMoney())
                .shippingMethod(orderDTO.getShippingMethod())
                .shippingAddress(orderDTO.getShippingAddress())
                .shippingDate(orderDTO.getShippingDate() != null ? orderDTO.getShippingDate() : LocalDate.now())
                .paymentMethod(orderDTO.getPaymentMethod())
                .active(true)
                .build();

        orderRepository.save(order);

        // Tạo OrderDetail
        List<OrderDetail> orderDetails = new ArrayList<>();
        for(CartItemDTO cartItemDTO : orderDTO.getCartItems()) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);

            Long productId = cartItemDTO.getProductId();
            int quantity = cartItemDTO.getQuantity();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new DataNotFoundException(
                            translate(MessageKeys.PRODUCT_NOT_FOUND, productId))
                    );

            orderDetail.setProduct(product);
            orderDetail.setNumberOfProducts(quantity);
            orderDetail.setPrice(product.getPrice());

            orderDetails.add(orderDetail);
        }
        order.setOrderDetails(orderDetails);
        orderDetailRepository.saveAll(orderDetails);

        // Chuyển Order sang OrderResponse
        return OrderResponse.fromOrder(order);
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, OrderDTO orderDTO) {
        try {
            Order existingOrder = orderRepository.findById(id).orElseThrow(
                    () -> new DataNotFoundException("Cannot find order with id: " + id));
            User existingUser = userRepository.findById(orderDTO.getUserId()).orElseThrow(
                    () -> new DataNotFoundException("Cannot find user with id: " + orderDTO.getUserId()));

            existingOrder.setUser(existingUser);
            existingOrder.setFullName(orderDTO.getFullName());
            existingOrder.setEmail(orderDTO.getEmail());
            existingOrder.setStatus(orderDTO.getStatus());
            existingOrder.setPhoneNumber(orderDTO.getPhoneNumber());
            existingOrder.setAddress(orderDTO.getAddress());
            existingOrder.setNote(orderDTO.getNote());
            existingOrder.setTotalMoney(orderDTO.getTotalMoney());
            existingOrder.setShippingMethod(orderDTO.getShippingMethod());
            existingOrder.setShippingAddress(orderDTO.getShippingAddress());
            existingOrder.setShippingDate(orderDTO.getShippingDate());
            existingOrder.setPaymentMethod(orderDTO.getPaymentMethod());

            return orderRepository.save(existingOrder);
        } catch (DataNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Order> findAllOrders() {
        // Lấy tất cả orders active = true
        return orderRepository.findAllByActiveTrue();
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.findById(id).ifPresent(order -> {
            order.setActive(false); // chỉ thay đổi flag
            orderRepository.save(order); // Hibernate sẽ update, không insert
        });
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}