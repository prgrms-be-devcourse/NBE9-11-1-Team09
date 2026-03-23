package com.back.domain.order.service;

import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.exception.OrderNotFoundException;
import com.back.domain.order.exception.OrderStatementNotFoundException;
import com.back.domain.order.repository.OrderItemRepository;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.order.repository.OrderStatementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatementRepository orderStatementRepository;

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
