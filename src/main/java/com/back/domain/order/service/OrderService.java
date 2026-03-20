package com.back.domain.order.service;

import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.entity.OrderStatement;
import com.back.domain.order.repository.OrderItemRepository;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.order.repository.OrderStatementRepository;
import java.util.NoSuchElementException;
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


    public OrderStatement removeStatementById(int orderId, int orderStatementId) {
        CoffeeOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("존재하지않는 orderId입니다."));
        OrderStatement removed = order.removeOrderStatement(orderStatementId)
                .orElseThrow(() -> new NoSuchElementException(("존재하지않는 주문ID입니다.")));

        if (order.getStatements().isEmpty()) {
            orderRepository.deleteById(orderId);
        }

        return removed;
    }
}
