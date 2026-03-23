package com.back.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.back.domain.order.dto.common.orderitem.OrderItemRequestDto;
import com.back.domain.order.dto.common.orderstatement.OrderStatementRequestDto;
import com.back.domain.order.dto.update.OrderUpdateRequestDto;
import com.back.domain.order.dto.update.OrderUpdateResponseDto;
import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;

    private CoffeeOrder savedOrder;
    private Product coffeeBean;

    @BeforeEach
    void setUp() {
        // 테스트용 기본 데이터 세팅
        coffeeBean = productRepository.save(new Product("고급 맛난 커피", 15000, 100));
        savedOrder = orderRepository.save(new CoffeeOrder("dnclsehd122@gmail.com"));
    }

    @Test
    @DisplayName("기존 주문 수정")
    void updateOrderWithArray_Success() {
        // 2개의 주문서가 담긴 배열 생성
        OrderItemRequestDto[] items = {
                new OrderItemRequestDto(0, coffeeBean.getId(), 2)
        };

        OrderStatementRequestDto statement1 = new OrderStatementRequestDto(0, "창원시 성산구", "51427", items);
        OrderStatementRequestDto statement2 = new OrderStatementRequestDto(0, "세종특별자치시 조치원읍", "30016", items);

        OrderStatementRequestDto[] statementArray = { statement1, statement2 };

        OrderUpdateRequestDto requestDto = new OrderUpdateRequestDto(
                savedOrder.getId(),
                "dnclsehd122@gmail.com",
                statementArray
        );

        OrderUpdateResponseDto response = orderService.updateOrder(savedOrder.getId(), requestDto);
        CoffeeOrder updatedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();

        // 주문서가 2개로 수정되었는지 확인
        assertThat(updatedOrder.getStatements()).hasSize(2);

        // 주문서 주소가 맞는지 확인
        assertThat(updatedOrder.getStatements().get(0).getAddress()).isEqualTo("창원시 성산구");

        // 응답 값 확인
        assertThat(response.orderId()).isEqualTo(savedOrder.getId());
    }
}