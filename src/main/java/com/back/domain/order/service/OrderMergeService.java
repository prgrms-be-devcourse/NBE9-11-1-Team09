package com.back.domain.order.service;

import com.back.domain.order.dto.create.OrderCreateRequestDto;
import com.back.domain.order.dto.merge.OrderMergeResponseDto;
import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.entity.OrderStatement;
import com.back.domain.order.repository.OrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderMergeService {

    private final OrderService orderService; // 기존 서비스
    private final OrderRepository orderRepository;

    @Transactional
    public OrderMergeResponseDto createOrMergeOrderWithTotalAmount(OrderCreateRequestDto requestDto) {

        orderService.createNewOrder(requestDto.email(), requestDto.orderStatements());

        CoffeeOrder order = orderRepository.findByEmail(requestDto.email())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        List<OrderStatement> statements = order.getStatements();
        OrderStatement latestStatement = statements.get(statements.size() - 1);

        return OrderMergeResponseDto.from(latestStatement, requestDto.email());
    }
}