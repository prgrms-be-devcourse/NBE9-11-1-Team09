package com.back.domain.order.service;

import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.repository.OrderItemRepository;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.order.repository.OrderStatementRepository;
import com.back.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import com.back.domain.order.exception.OrderNotFoundException;
import com.back.domain.order.exception.OrderStatementNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatementRepository orderStatementRepository;
    private final ProductService productService;

    @Transactional
    public CoffeeOrder createOrder(String email, OrderStatementRequestDto orderStatements) {
        if (orderRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 주문된 이메일입니다", HttpStatus.BAD_REQUEST);
        }
        CoffeeOrder order = new Order(email, orderStatements);
        return orderRepository.save(order);
    }

    @Transactional
    public OrderStatement setDeliveryInfo(String address, String zipCode, Order existingOrder) {
        OrderStatement orderStatement = new OrderStatement(address, zipCode, existingOrder);
        return orderStatementRepository.save(orderStatement);
    }

    @Transactional
    public OrderItem addMenu(OrderStatement orderStatement, Product product, int quantity) {
        OrderItem orderItem = new OrderItem(orderStatement, product, quantity);
        return orderItemRepository.save(orderItem);
    }


    public long count() {
        return orderRepository.count();
    }

    @Transactional(readOnly = true)
    public List<CoffeeOrder> findAll() {
        return orderRepository.findAll();
    }

    // order id 로 찾기 & 찼았는데 -> 없는 경우 (예외처리)
    public CoffeeOrder findById(int id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        id + " Order not found"
                ));
    }

    public void removeStatementById(int orderId, int orderStatementId) {
        CoffeeOrder order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        order.removeOrderStatement(orderStatementId)
                .orElseThrow(OrderStatementNotFoundException::new);

        if (order.getStatements().isEmpty()) {
            orderRepository.deleteById(orderId);
        }

    }
}
