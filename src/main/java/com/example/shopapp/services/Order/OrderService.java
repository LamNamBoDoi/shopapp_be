package com.example.shopapp.services.Order;

import com.example.shopapp.dtos.OrderDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Order;
import com.example.shopapp.models.OrderStatus;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.OrderRepository;
import com.example.shopapp.repositories.UserRepository;
import com.example.shopapp.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

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
        orderRepository.save(order);
        return modelMapper.map(order, OrderResponse.class);
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
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


