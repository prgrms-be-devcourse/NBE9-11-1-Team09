package com.back.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    void updateOrder() {
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

    @Test
    @DisplayName("404 에러: 존재하지 않는 주문 ID 수정")
    void updateOrderNotFound() {
        // 존재하지 않는 임의의 주문 ID 값
        int invalidId = 31785463;
        OrderUpdateRequestDto requestDto = new OrderUpdateRequestDto(invalidId, "dnclsehd123@naver.com", null);

        assertThatThrownBy(() -> orderService.updateOrder(invalidId, requestDto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(rse.getReason()).contains("수정할 " + invalidId + "번 주문서를 찾을 수 없습니다.");
                });
    }

    @Test
    @DisplayName("400 에러: 수량 양식 위반")
    void updateOrderBadRequest() {
        OrderItemRequestDto[] items = {
                new OrderItemRequestDto(0, coffeeBean.getId(), 0)
        };

        OrderStatementRequestDto[] statements = {
                new OrderStatementRequestDto(0, "창원시 성산구", "51427", items)
        };

        OrderUpdateRequestDto requestDto = new OrderUpdateRequestDto(
                savedOrder.getId(),
                "dnclsehd122@gmail.com",
                statements
        );

        assertThatThrownBy(() -> orderService.updateOrder(savedOrder.getId(), requestDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("404 에러: 존재하지 않는 상품 포함")
    void updateOrderProductNotFound() {
        // 존재하지 않는 임의의 상품 ID 값
        int invalidId = 31785463;
        OrderItemRequestDto[] items = {
                new OrderItemRequestDto(0, invalidId, 1)
        };

        OrderStatementRequestDto[] statements = {
                new OrderStatementRequestDto(0, "창원시 성산구", "51427", items)
        };

        OrderUpdateRequestDto requestDto = new OrderUpdateRequestDto(
                savedOrder.getId(),
                "dnclsehd122@gmail.com",
                statements
        );

        assertThatThrownBy(() -> orderService.updateOrder(savedOrder.getId(), requestDto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(rse.getReason()).contains(invalidId + " 번 상품을 찾을 수 없습니다.");
                });
    }

    @Test
    @DisplayName("400 에러: 입력 데이터 검증 (경로 ID와 DTO 내부 ID 불일치)")
    void updateOrderIdMismatch() {
        // 존재하지 않는 임의의  경로 ID 값
        int invalidId = 31785463;
        OrderUpdateRequestDto requestDto = new OrderUpdateRequestDto(
                savedOrder.getId() + invalidId, // 경로 ID는 savedOrder.getId(), DTO ID는 경로 ID의 값 + invalidId로 설정
                "dnclsehd122@gmail.com",
                null
        );

        assertThatThrownBy(() -> orderService.updateOrder(savedOrder.getId(), requestDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }
}