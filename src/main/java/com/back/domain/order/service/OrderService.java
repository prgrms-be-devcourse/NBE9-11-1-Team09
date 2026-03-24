package com.back.domain.order.service;

import com.back.domain.order.dto.create.OrderCreateRequestDto;
import com.back.domain.order.dto.create.OrderCreateResponseDto;
import com.back.domain.order.dto.orderitem.OrderItemRequestDto;
import com.back.domain.order.dto.orderstatement.OrderStatementRequestDto;
import com.back.domain.order.dto.update.OrderUpdateRequestDto;
import com.back.domain.order.dto.update.OrderUpdateResponseDto;
import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.entity.OrderStatement;
import com.back.domain.order.exception.OrderNotFoundException;
import com.back.domain.order.exception.OrderStatementNotFoundException;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.order.repository.OrderStatementRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.repository.ProductRepository;
import com.back.domain.product.service.ProductService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderStatementRepository orderStatementRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

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
    public OrderCreateResponseDto createNewOrder(String email, OrderStatementRequestDto requestDto) {
        // 주문 조회 및 없을시 새로 생성
        CoffeeOrder coffeeOrder = orderRepository.findByEmail(email)
                .orElseGet(() -> new CoffeeOrder(email));
        // 주문 명세 추가
        OrderStatement orderStatement = coffeeOrder.addOrderStatement(
                requestDto.address(),
                requestDto.zipCode()
        );

        for (OrderItemRequestDto itemDto : requestDto.orderItems()) {
            Product product = productService.productExists(itemDto.productId());
            orderStatement.addOrderItem(itemDto.quantity(), product);
        }

        CoffeeOrder savedOrder = orderRepository.save(coffeeOrder);
        return OrderCreateResponseDto.from(savedOrder);
    }


    @Transactional
    public void removeStatementById(int orderId, int orderStatementId) {
        CoffeeOrder order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        order.removeOrderStatement(orderStatementId)
                .orElseThrow(OrderStatementNotFoundException::new);

        if (order.getStatements().isEmpty()) {
            orderRepository.deleteById(orderId);
        }

    }

    @Transactional
    public OrderUpdateResponseDto updateOrder(
            int orderId,
            int orderStatementId,
            OrderUpdateRequestDto requestDto) {
        CoffeeOrder coffeeOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "수정할 " + orderId + "번 주문을 찾을 수 없습니다."
                ));

        OrderStatement prevStatement = orderStatementRepository.findById(orderStatementId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "수정할 " + orderStatementId + "번 주문서을 찾을 수 없습니다."
                ));

        OrderStatement renewStatement = new OrderStatement(
                requestDto.orderStatement().address(),
                requestDto.orderStatement().zipCode(),
                coffeeOrder
        );

        OrderItemRequestDto[] orderItems = requestDto.orderStatement().orderItems();

        for (OrderItemRequestDto item : orderItems) {
            Product p = productRepository.findById(item.productId()).orElseThrow(
                    () -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "수정된 주문서에 있는 제품을 현재 찾을 수 없습니다. id : %d".formatted(item.productId())
                    )
            );
            renewStatement.addOrderItem(item.quantity(), p);
        }

        coffeeOrder.removeOrderStatement(orderStatementId);

        coffeeOrder.getStatements().add(renewStatement);

        OrderStatement saved = orderStatementRepository.save(renewStatement);

        return OrderUpdateResponseDto.from(saved, coffeeOrder.getEmail());
    }

    @Transactional
    public OrderUpdateResponseDto createOrMergeOrder(OrderCreateRequestDto requestDto) {
        // 현재 배송 배치(오후 2시 기준)의 시작과 끝 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startRange = now.withHour(14).withMinute(0).withSecond(0).withNano(0);

        // 현재가 14시 전이라면 시작은 어제 14시, 끝은 오늘 14시
        // 현재가 14시 후라면 시작은 오늘 14시, 끝은 내일 14시
        if (now.isBefore(startRange)) {
            startRange = startRange.minusDays(1);
        }
        LocalDateTime endRange = startRange.plusDays(1);

        // 해당 범위 내에 동일 이메일의 주문이 있는지 확인
        CoffeeOrder order = orderRepository.findByEmailAndCreateDateBetween(
                        requestDto.email(), startRange, endRange)
                .orElseGet(() -> {
                    CoffeeOrder newOrder = new CoffeeOrder(requestDto.email());
                    return orderRepository.save(newOrder);
                });

        // 주문 내용 추가 (새로운 주문이든 기존 주문이든 동일하게 처리)
        var statementDto = requestDto.orderStatements();
        OrderStatement statement = order.addOrderStatement(statementDto.address(), statementDto.zipCode());

        for (var itemDto : statementDto.orderItems()) {
            Product product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, itemDto.productId() + "번 상품은 존재하지 않습니다."));
            statement.addOrderItem(itemDto.quantity(), product);
        }

        return OrderUpdateResponseDto.from(statement, order.getEmail());
    }
}