package com.back.domain.order.service;

import com.back.domain.order.dto.common.orderitem.OrderItemRequestDto;
import com.back.domain.order.dto.common.orderstatement.OrderStatementRequestDto;
import com.back.domain.order.dto.update.OrderUpdateRequestDto;
import com.back.domain.order.dto.update.OrderUpdateResponseDto;
import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.entity.OrderStatement;
import com.back.domain.order.repository.OrderItemRepository;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.order.repository.OrderStatementRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatementRepository orderStatementRepository;
    private final ProductRepository productRepository;

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

    @Transactional
    public OrderUpdateResponseDto updateOrder(int id, OrderUpdateRequestDto requestDto) {
        // 기존 주문 조회
        CoffeeOrder coffeeOrder = orderRepository.findById(id)
                // 없는 주문서에 대한 수정 시도 시 404 에러 발생
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "수정할 " + id + "번 주문서를 찾을 수 없습니다."
                ));

        // 입력 데이터 검증
        if (requestDto.id() != id) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청 ID가 일치하지 않습니다.");
        }

        // 기존 주문서 내역 삭제
        coffeeOrder.getStatements().clear();

        // 주문서 배열 처리
        if (requestDto.orderStatements() == null || requestDto.orderStatements().length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "최소 하나 이상의 주문서 내용이 필요합니다.");
        }

        // 배열을 순회하며 새로운 단일 주문서 추가
        for (OrderStatementRequestDto statementDto : requestDto.orderStatements()) {
            // 주문서 추가
            OrderStatement statement = coffeeOrder.addOrderStatement(
                    statementDto.address(),
                    statementDto.zipCode()
            );

            // 새로운 상품 목록 추가
            for (OrderItemRequestDto itemDto : statementDto.orderItems()) {
                if (itemDto.quantity() <= 0) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "상품 수량은 최소 1개 이상이어야 합니다.");
                }

                Product product = productRepository.findById(itemDto.productId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                itemDto.productId() + " 번 상품을 찾을 수 없습니다."
                        ));

                statement.addOrderItem(itemDto.quantity(), product);
            }
        }

        return OrderUpdateResponseDto.from(coffeeOrder);
    }
}