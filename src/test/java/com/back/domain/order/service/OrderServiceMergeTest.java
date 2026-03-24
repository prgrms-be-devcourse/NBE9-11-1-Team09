package com.back.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

import com.back.domain.order.dto.create.OrderCreateRequestDto;
import com.back.domain.order.dto.orderitem.OrderItemRequestDto;
import com.back.domain.order.dto.orderstatement.OrderStatementRequestDto;
import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.repository.ProductRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceMergeTest {

    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;

    private Product coffeeBean;

    @BeforeEach
    void setUp() {
        // 테스트용 기본 데이터 세팅
        coffeeBean = productRepository.save(new Product("고급 더욱 맛난 커피", 15000, 100));
    }

    @Test
    @DisplayName("같은 배송 타임라인에 같은 이메일로 2번 주문하면 1개의 주문으로 병합")
    void mergeOrderBefore2PM() {
        String email = "dnclsehd122@gmail.com";
        OrderItemRequestDto[] items = { new OrderItemRequestDto(coffeeBean.getId(), 2) };
        OrderStatementRequestDto statement1 = new OrderStatementRequestDto("경상남도 창원시", "51427", items);
        OrderStatementRequestDto statement2 = new OrderStatementRequestDto("세종특별자치시 조치원읍", "30016", items);

        OrderCreateRequestDto request1 = new OrderCreateRequestDto(email, statement1);
        OrderCreateRequestDto request2 = new OrderCreateRequestDto(email, statement2);

        // 현재 시간
        LocalDateTime fixedTime = LocalDateTime.now();

        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
            mockedTime.when(LocalDateTime::now).thenReturn(fixedTime);

            orderService.createOrMergeOrder(request1);
            orderService.createOrMergeOrder(request2);
        }

        List<CoffeeOrder> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatements()).hasSize(2);
        assertThat(orders.get(0).getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("배송 컷오프(오후 2시)를 넘겨서 주문하면 병합되지 않고 2개의 주문으로 분리")
    void notMergeOrderAcross2PM() {
        String email = "dnclsehd122@gmail.com";
        OrderItemRequestDto[] items = { new OrderItemRequestDto(coffeeBean.getId(), 2) };
        OrderStatementRequestDto statement = new OrderStatementRequestDto("경상남도 창원시", "51427", items);
        OrderCreateRequestDto request = new OrderCreateRequestDto(email, statement);

        // 첫 번째 주문은 현재 시간에서 진행
        LocalDateTime batch1Time = LocalDateTime.now();
        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
            mockedTime.when(LocalDateTime::now).thenReturn(batch1Time);
            orderService.createOrMergeOrder(request);
        }

        // 두 번째 주문은 현재 시간보다 하루 뒤에서 진행
        LocalDateTime batch2Time = batch1Time.plusDays(1);
        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
            mockedTime.when(LocalDateTime::now).thenReturn(batch2Time);
            orderService.createOrMergeOrder(request);
        }

        List<CoffeeOrder> orders = orderRepository.findAll();
        assertThat(orders).hasSize(2);
    }

    @Test
    @DisplayName("404 에러: 존재하지 않는 상품으로 주문 생성 시도")
    void createOrderProductNotFound() {
        OrderItemRequestDto[] items = { new OrderItemRequestDto(9999, 1) };
        OrderStatementRequestDto statement = new OrderStatementRequestDto("경상남도 창원시", "51427", items);
        OrderCreateRequestDto requestDto = new OrderCreateRequestDto("dnclsehd122@gmail.com", statement);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.createOrMergeOrder(requestDto)
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(exception.getReason()).contains("9999번 상품은 존재하지 않습니다.");
    }

    @Test
    @DisplayName("성공: 이메일이 다르면 같은 시간에 주문해도 병합되지 않음")
    void notMergeDifferentEmail() {
        OrderItemRequestDto[] items = { new OrderItemRequestDto(coffeeBean.getId(), 1) };
        OrderStatementRequestDto statement = new OrderStatementRequestDto("경상남도 창원시", "51427", items);

        OrderCreateRequestDto request1 = new OrderCreateRequestDto("dnclsehd122@gmail.com", statement);
        OrderCreateRequestDto request2 = new OrderCreateRequestDto("dnclsehd123@naver.com", statement);

        // 현재 시간으로 고정
        LocalDateTime fixedTime = LocalDateTime.now();
        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
            mockedTime.when(LocalDateTime::now).thenReturn(fixedTime);

            orderService.createOrMergeOrder(request1);
            orderService.createOrMergeOrder(request2);
        }

        List<CoffeeOrder> orders = orderRepository.findAll();
        assertThat(orders).hasSize(2);
    }
}