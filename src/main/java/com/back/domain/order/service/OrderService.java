package com.back.domain.order.service;

import com.back.domain.order.dto.common.orderitem.OrderItemRequestDto;
import com.back.domain.order.dto.common.orderstatement.OrderStatementRequestDto;
import com.back.domain.order.dto.create.OrderCreateResponseDto;
import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.entity.OrderStatement;
import com.back.domain.order.exception.OrderNotFoundException;
import com.back.domain.order.exception.OrderStatementNotFoundException;
import com.back.domain.order.repository.OrderItemRepository;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.order.repository.OrderStatementRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatementRepository orderStatementRepository;
    private final ProductService productService;

    @Transactional
    public OrderCreateResponseDto createOrder(String email, OrderStatementRequestDto requestDto) {
        if (orderRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 주문된 이메일입니다");
        }
        CoffeeOrder coffeeOrder = new CoffeeOrder(email);
        OrderStatement orderStatement = new OrderStatement(requestDto.address(), requestDto.zipCode(), coffeeOrder);
        // 관계 설정
        coffeeOrder.getStatements().add(orderStatement);

        for (OrderItemRequestDto itemDto : requestDto.orderItems()) {
            Product product = productService.productExists(itemDto.productId());
            // 관계 설정
            orderStatement.addOrderItem(itemDto.quantity(), product);
        }
        CoffeeOrder saveCoffeeOrder = orderRepository.save(coffeeOrder);
        return OrderCreateResponseDto.from(saveCoffeeOrder);
    }


    public long count() {
        return orderRepository.count();
    }

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
