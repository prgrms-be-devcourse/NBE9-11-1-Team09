package com.back.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.back.domain.order.dto.create.OrderCreateRequestDto;
import com.back.domain.order.dto.merge.OrderMergeResponseDto;
import com.back.domain.order.dto.orderitem.OrderItemRequestDto;
import com.back.domain.order.dto.orderstatement.OrderStatementRequestDto;
import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.entity.OrderStatement;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderTotalAmountTest {

    @Autowired private OrderMergeService orderMergeService;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private EntityManager em;

    private Product goodCoffee;
    private Product betterCoffee;

    @BeforeEach
    void setUp() {
        // 테스트용 기본 데이터 세팅
        goodCoffee = productRepository.save(new Product("고급 맛난 커피", 3000, 100));
        betterCoffee = productRepository.save(new Product("최최고급 맛난 커피", 4500, 100));

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("단일 상품 주문 시 총 금액 계산")
    void calculateTotalAmount_SingleProduct() {
        // 고급 맛난 커피 3잔 (3000 * 3 = 9000)
        OrderItemRequestDto[] items = {
                new OrderItemRequestDto(goodCoffee.getId(), 3)
        };
        OrderStatementRequestDto statement = new OrderStatementRequestDto("창원시 성산구", "51427", items);
        OrderCreateRequestDto request = new OrderCreateRequestDto("dnclsehd122@gmail.com", statement);

        OrderMergeResponseDto response = orderMergeService.createOrMergeOrderWithTotalAmount(request);

        assertThat(response.totalAmount()).isEqualTo(9000);
    }

    @Test
    @DisplayName("여러 상품 주문 시 총 금액의 합계 계산")
    void calculateTotalAmount_MultipleProducts() {
        // 고급 맛난 커피 2잔(6000) + 최최고급 맛난 커피 3잔(13500) = 19500
        OrderItemRequestDto[] items = {
                new OrderItemRequestDto(goodCoffee.getId(), 2),
                new OrderItemRequestDto(betterCoffee.getId(), 3)
        };
        OrderStatementRequestDto statement = new OrderStatementRequestDto("창원시 성산구", "51427", items);
        OrderCreateRequestDto request = new OrderCreateRequestDto("dnclsehd122@gmail.com", statement);

        OrderMergeResponseDto response = orderMergeService.createOrMergeOrderWithTotalAmount(request);

        assertThat(response.totalAmount()).isEqualTo(19500);
    }

    @Test
    @DisplayName("주문이 병합될 때 전체 주문의 총 금액 계산")
    void calculateTotalAmount_MergedOrder() {
        // 이메일 설정
        String email = "dnclsehd122@gmail.com";

        // 첫 번째 주문: 고급 맛난 커피 1잔 (3000)
        OrderItemRequestDto[] item1 = { new OrderItemRequestDto(goodCoffee.getId(), 1) };
        OrderCreateRequestDto request1 = new OrderCreateRequestDto(email,
                new OrderStatementRequestDto("창원시 성산구", "51427", item1));

        orderMergeService.createOrMergeOrderWithTotalAmount(request1);
        em.flush();
        em.clear();

        // 두 번째 주문 (병합 발생): 최최고급 맛난 커피 1잔 (4500)
        OrderItemRequestDto[] item2 = { new OrderItemRequestDto(betterCoffee.getId(), 1) };
        OrderCreateRequestDto request2 = new OrderCreateRequestDto(email,
                new OrderStatementRequestDto("창원시 성산구", "51427", item2));

        OrderMergeResponseDto response2 = orderMergeService.createOrMergeOrderWithTotalAmount(request2);

        // 3000(기존) + 4500(신규) = 7500
        assertThat(response2.totalAmount()).isEqualTo(7500);

        // 데이터베이스 상태 검증
        CoffeeOrder finalOrder = orderRepository.findByEmail(email).orElseThrow();
        assertThat(finalOrder.getStatements()).hasSize(2);

        int dbTotalAmount = finalOrder.getStatements().stream()
                .mapToInt(OrderStatement::getTotalAmount)
                .sum();
        assertThat(dbTotalAmount).isEqualTo(7500);
    }

    @Test
    @DisplayName("예외: 존재하지 않는 상품 ID로 주문 시 예외 처리")
    void calculateTotalAmount_ProductNotFound() {
        // 존재하지 않는 productID (9999)
        int productId = 9999;
        var invalidItem = new OrderItemRequestDto(productId, 1);
        var statement = new OrderStatementRequestDto("창원시 성산구", "51427", new OrderItemRequestDto[]{invalidItem});
        var request = new OrderCreateRequestDto("창원시 성산구", statement);

        assertThatThrownBy(() -> orderMergeService.createOrMergeOrderWithTotalAmount(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(productId + "번 상품은 존재하지 않습니다");
    }
}