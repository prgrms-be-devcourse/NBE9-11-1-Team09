package com.back.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.back.domain.order.dto.orderitem.OrderItemRequestDto;
import com.back.domain.order.dto.orderstatement.OrderStatementRequestDto;
import com.back.domain.order.dto.update.OrderUpdateRequestDto;
import com.back.domain.order.dto.update.OrderUpdateResponseDto;
import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.entity.OrderStatement;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.order.repository.OrderStatementRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceTest {

    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderStatementRepository orderStatementRepository;
    @Autowired private ProductRepository productRepository;

    private CoffeeOrder savedOrder;
    private OrderStatement prevOrderStatement;
    private Product coffeeBean;

    @BeforeEach
    void setUp() {
        // 테스트용 기본 데이터 세팅
        coffeeBean = productRepository.save(new Product("고급 맛난 커피", 15000, 100));
        savedOrder = orderRepository.save(new CoffeeOrder("dnclsehd122@gmail.com"));
        prevOrderStatement = new OrderStatement("test", "test", savedOrder);
        savedOrder.getStatements().add(prevOrderStatement);
        prevOrderStatement = orderStatementRepository.save(prevOrderStatement);
    }

    @Test
    @DisplayName("기존 주문 수정")
    void updateOrder() {
        // 2개의 주문서가 담긴 배열 생성
        OrderItemRequestDto[] items = {
                new OrderItemRequestDto(coffeeBean.getId(), 2)
        };

        OrderStatementRequestDto statement = new OrderStatementRequestDto("창원시 성산구", "51427", items);

        OrderUpdateRequestDto requestDto = new OrderUpdateRequestDto(
                "dnclsehd122@gmail.com",
                statement
        );

        OrderUpdateResponseDto response = orderService.updateOrder(savedOrder.getId(), prevOrderStatement.getId(), requestDto);
        CoffeeOrder updatedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
        OrderStatement updatedOrderStatement = orderStatementRepository.findById(response.orderStatementId()).orElseThrow();

        // 주문서가 1개로 유지되는지 확인
        assertThat(updatedOrder.getStatements()).hasSize(1);

        // 주문서 주소가 맞는지 확인
        assertThat(updatedOrder.getStatements().get(0).getAddress()).isEqualTo("창원시 성산구");

        // 응답 값 확인
        assertThat(response.orderStatementId()).isEqualTo(updatedOrderStatement.getId());
    }

    @Test
    @DisplayName("404 에러: 존재하지 않는 주문 ID 수정")
    void updateOrderNotFound() {
        // Given: 존재하지 않는 orderId (9999)
        OrderItemRequestDto[] items = {
                new OrderItemRequestDto(coffeeBean.getId(), 2)
        };
        OrderStatementRequestDto statement = new OrderStatementRequestDto("서울시 강남구", "06000", items);
        OrderUpdateRequestDto requestDto = new OrderUpdateRequestDto("test@email.com", statement);

        // When & Then: NOT_FOUND 예외 발생 확인
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.updateOrder(9999, prevOrderStatement.getId(), requestDto)
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).contains("9999번 주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("404 에러: 존재하지 않는 주문서 ID 수정")
    void updateOrderStatementNotFound() {
        // Given: 존재하지 않는 orderStatementId (9999)
        OrderItemRequestDto[] items = {
                new OrderItemRequestDto(coffeeBean.getId(), 2)
        };
        OrderStatementRequestDto statement = new OrderStatementRequestDto("부산시 해운대구", "48000", items);
        OrderUpdateRequestDto requestDto = new OrderUpdateRequestDto("dnclsehd122@gmail.com", statement);

        // When & Then: NOT_FOUND 예외 발생 확인
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.updateOrder(savedOrder.getId(), 9999, requestDto)
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).contains("9999번 주문서를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("400 에러: 존재하지 않는 상품 포함")
    void updateOrderProductNotFound() {
        // Given: 존재하지 않는 productId (9999) 를 주문서에 포함
        OrderItemRequestDto[] items = {
                new OrderItemRequestDto(9999, 2)  // ❌ 존재하지 않는 상품 ID
        };
        OrderStatementRequestDto statement = new OrderStatementRequestDto("대구시 중구", "41000", items);
        OrderUpdateRequestDto requestDto = new OrderUpdateRequestDto("dnclsehd122@gmail.com", statement);

        // When & Then: BAD_REQUEST 예외 발생 확인
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.updateOrder(savedOrder.getId(), prevOrderStatement.getId(), requestDto)
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).contains("수정된 주문서에 있는 제품을 현재 찾을 수 없습니다");
    }
}