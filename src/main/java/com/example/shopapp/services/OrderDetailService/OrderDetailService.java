package com.example.shopapp.services.OrderDetailService;

import com.example.shopapp.dtos.OrderDTO;
import com.example.shopapp.dtos.OrderDetailDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Order;
import com.example.shopapp.models.OrderDetail;
import com.example.shopapp.models.Product;
import com.example.shopapp.repositories.OrderDetailRepository;
import com.example.shopapp.repositories.OrderRepository;
import com.example.shopapp.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderDetailService implements IOrderDetailService {
    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) {
        // tim xem order co ton tai ko
        try {
            Order order = orderRepository.findById(orderDetailDTO.getOrderId()).orElseThrow(
                    () -> new DataNotFoundException("Cannot find order with id: "+orderDetailDTO.getOrderId())
            );
            Product product = productRepository.findById(orderDetailDTO.getProductId()).orElseThrow(
                    () -> new DataNotFoundException("Cannot find product with id: "+orderDetailDTO.getProductId())
            );
            OrderDetail orderDetail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .numberOfProducts(orderDetailDTO.getNumberOfProducts())
                    .totalMoney(orderDetailDTO.getTotalMoney())
                    .color(orderDetailDTO.getColor())
                    .build();
            return orderDetailRepository.save(orderDetail);
        } catch (DataNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OrderDetail getOrderDetail(Long id) {
        try {
            return orderDetailRepository.findById(id).orElseThrow(
                    ()->new DataNotFoundException("Cannot find OrderDetail with id: "+ id)
            );
        } catch (DataNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public OrderDetail updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO) throws DataNotFoundException {
            // Tìm order detail có tồn tại không
            OrderDetail existingOrderDetail = orderDetailRepository.findById(id).orElseThrow(
                    () -> new DataNotFoundException("Cannot find order detail with id: " + id)
            );

            // Tìm order có tồn tại không
            Order existingOrder = orderRepository.findById(orderDetailDTO.getOrderId()).orElseThrow(
                    () -> new DataNotFoundException("Cannot find order with id: " + orderDetailDTO.getOrderId())
            );

            // Tìm product có tồn tại không
            Product existingProduct = productRepository.findById(orderDetailDTO.getProductId()).orElseThrow(
                    () -> new DataNotFoundException("Cannot find product with id: " + orderDetailDTO.getProductId())
            );

            existingOrderDetail.setPrice(orderDetailDTO.getPrice());
            existingOrderDetail.setNumberOfProducts(orderDetailDTO.getNumberOfProducts());
            existingOrderDetail.setTotalMoney(orderDetailDTO.getTotalMoney());
            existingOrderDetail.setColor(orderDetailDTO.getColor());
            existingOrderDetail.setOrder(existingOrder);
            existingOrderDetail.setProduct(existingProduct);
            return orderDetailRepository.save(existingOrderDetail);
    }


    @Override
    public void deleteOrderDetail(Long id) {
        orderDetailRepository.deleteById(id);
    }

    @Override
    public List<OrderDetail> findByOrderId(Long orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }
}
