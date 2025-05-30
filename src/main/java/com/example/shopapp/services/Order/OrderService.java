package com.example.shopapp.services.Order;

import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.dtos.CartItemDTO;
import com.example.shopapp.dtos.OrderDTO;
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
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService extends TranslateMessages implements IOrderService{
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    public Page<OrderResponse> findByKeyword(String keyword, Pageable pageable) {
        // lấy danh sách sản phẩm theo trang(page) và giới hạn(limit)
        Page<Order> orderPage;
        orderPage = orderRepository.findByKeyword(keyword, pageable);
        return orderPage.map(OrderResponse::fromOrder);
    }

    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    public OrderResponse createOrder(OrderDTO orderDTO) throws DataNotFoundException {
        // tìm kiếm user id có tồn tại ko
       User user = userRepository.findById(orderDTO.getUserId()).orElseThrow(
               ()->new DataNotFoundException("Can't find user with id: "+ orderDTO.getUserId())
       );

       // convert orderDTO sang order
        // dùng thư viện model mapper
        // tạo 1 luồng bằng ánh xạ riêng để kiểm soát việc ảnh xạ
        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));

        // cap nhat cac truong cua don hang tu orderDTO
        Order order = new Order();
        modelMapper.map(orderDTO, order);
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.PENDING);

        // kiem tra shipping date phai >= ngay hom nay
        LocalDate shippingDate = orderDTO.getShippingDate() == null ? LocalDate.now(): orderDTO.getShippingDate();
        if(shippingDate.isBefore(LocalDate.now())){
            throw new DataNotFoundException("Date must be at least today!");
        }
        order.setShippingDate(shippingDate);
        order.setActive(true);
        order.setTotalMoney(orderDTO.getTotalMoney());
        orderRepository.save(order);

        // tạo danh sách các đối tượng orderDetails
        List<OrderDetail> orderDetails = new ArrayList<>();
        for(CartItemDTO cartItemDTO : orderDTO.getCartItems()){
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);

            // lấy thông in sản phẩm từ cartItemDto
            Long productId = cartItemDTO.getProductId();
            int quantity = cartItemDTO.getQuantity();

            // tìm thông tin sản phẩm từ cơ sở dữ liệu
            Product product = productRepository.findById(productId)
                    .orElseThrow(()->new DataNotFoundException(
                            translate(MessageKeys.PRODUCT_NOT_FOUND, productId)
                    ));
            // Đặt thông tin cho orderDetails
            orderDetail.setProduct(product);
            orderDetail.setNumberOfProducts(quantity);
            orderDetail.setPrice(product.getPrice());

            // thêm orderDetails vào danh sách
            orderDetails.add(orderDetail);
        }
        orderDetailRepository.saveAll(orderDetails);
        return modelMapper.map(order, OrderResponse.class);
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
                    () -> new DataNotFoundException("Cannot find order with id: "+id));
            User existingUser = userRepository.findById(orderDTO.getUserId()).orElseThrow(
                    () -> new DataNotFoundException("Cannot find user with id: "+id));
            modelMapper.typeMap(OrderDTO.class, Order.class)
                    .addMappings(mapper->mapper.skip(Order::setId));
            modelMapper.map(orderDTO, existingOrder);
            existingOrder.setUser(existingUser);
            return orderRepository.save(existingOrder);
        } catch (DataNotFoundException e) {
            throw new RuntimeException(e);
        }
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


