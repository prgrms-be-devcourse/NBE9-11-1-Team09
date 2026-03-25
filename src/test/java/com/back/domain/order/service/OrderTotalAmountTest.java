package com.back.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.back.domain.order.dto.create.OrderCreateRequestDto;
import com.back.domain.order.dto.orderitem.OrderItemRequestDto;
import com.back.domain.order.dto.orderstatement.OrderStatementRequestDto;
import com.back.domain.order.dto.update.OrderUpdateResponseDto;
import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.repository.OrderItemRepository;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

;
;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderTotalAmountTest {

    @Autowired private OrderService orderService;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private EntityManager em;

    private Product goodCoffee;
    private Product betterCoffee;
    @Autowired
    private OrderRepository orderRepository;

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

        OrderUpdateResponseDto response = orderService.createOrMergeOrder(request);

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

        OrderUpdateResponseDto response = orderService.createOrMergeOrder(request);

        assertThat(response.totalAmount()).isEqualTo(19500);
    }

    @Test
    @DisplayName("주문이 병합될 때 전체 주문의 총 금액 계산")
    void calculateTotalAmount_MergedOrder() {
        // 고정할 시간 및 이메일 설정
        LocalDateTime fixedTime = LocalDateTime.now().withHour(10).withMinute(0);
        String email = "dnclsehd122@gmail.com";

        // 첫 번째 주문: 고급 맛난 커피 1잔 (3000)
        OrderItemRequestDto[] item1 = { new OrderItemRequestDto(goodCoffee.getId(), 1) };
        OrderCreateRequestDto request1 = new OrderCreateRequestDto(email,
                new OrderStatementRequestDto("창원시 성산구", "51427", item1));

        OrderUpdateResponseDto response1 = orderService.createOrMergeOrder(request1);

        em.flush();

        CoffeeOrder order1 = orderRepository.findAll().stream()
                .filter(coffeeOrder -> coffeeOrder.getEmail().equals(email))
                .findFirst()
                .orElseThrow();

        // 시간 고정 (오전 10시)
        ReflectionTestUtils.setField(order1, "createDate", fixedTime);

        orderRepository.saveAndFlush(order1);
        em.clear();

        // 두 번째 주문 (병합 발생): 최최고급 맛난 커피 1잔 (4500)
        OrderItemRequestDto[] item2 = { new OrderItemRequestDto(betterCoffee.getId(), 1) };
        OrderCreateRequestDto request2 = new OrderCreateRequestDto(email,
                new OrderStatementRequestDto("창원시 성산구", "51427", item2));

        OrderUpdateResponseDto response2 = orderService.createOrMergeOrder(request2);

        // 3000(기존) + 4500(신규) = 7500
        assertThat(response2.totalAmount()).isEqualTo(7500);

        // 데이터베이스 상태 검증
        CoffeeOrder finalOrder = orderRepository.findById(order1.getId()).orElseThrow();
        assertThat(finalOrder.getStatements()).hasSize(2);
        assertThat(finalOrder.getTotalAmount()).isEqualTo(7500);
    }

    @Test
    @DisplayName("예외: 존재하지 않는 상품 ID로 주문 시 예외 처리")
    void calculateTotalAmount_ProductNotFound() {
        // 존재하지 않는 productID (9999)
        int productId = 9999;
        var invalidItem = new OrderItemRequestDto(productId, 1);
        var statement = new OrderStatementRequestDto("창원시 성산구", "51427", new OrderItemRequestDto[]{invalidItem});
        var request = new OrderCreateRequestDto("창원시 성산구", statement);

        assertThatThrownBy(() -> orderService.createOrMergeOrder(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(productId + "번 상품은 존재하지 않습니다");
    }
}