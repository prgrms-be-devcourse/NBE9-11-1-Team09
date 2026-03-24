package com.back.domain.order.service;

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
}