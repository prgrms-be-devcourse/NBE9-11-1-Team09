package com.back.order.controller;

import com.back.domain.order.dto.common.orderitem.OrderItemRequestDto;
import com.back.domain.order.dto.common.orderstatement.OrderStatementRequestDto;
import com.back.domain.order.dto.create.OrderCreateRequestDto;
import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.factory.OrderTestDataFactory;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class OrderControllerCreateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    private Product savedProduct;

    @BeforeEach
    void setUp() {
        savedProduct = productRepository.save(
                new Product("아메리카노", 4500, 1)
        );
    }

    @Test
    @DisplayName("✅ [통합] 주문 생성 성공 - OrderService 비즈니스 로직 정상 수행")
    void createOrder_success_verifyServiceFlow() throws Exception {
        // given
        String testEmail = "integration@test.com";
        OrderItemRequestDto orderItem = new OrderItemRequestDto(
                0, savedProduct.getId(), 2
        );
        OrderStatementRequestDto statementDto = new OrderStatementRequestDto(
                0, "서울시 강남구", "12345", new OrderItemRequestDto[]{orderItem}
        );
        OrderCreateRequestDto requestDto = new OrderCreateRequestDto(testEmail, statementDto);
        String requestJson = objectMapper.writeValueAsString(requestDto);

        // when
        var result = mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(testEmail))
                .andReturn();

        // then - ✅ DB 직접 조회로 Service 의 save() 동작 검증
        assertThat(orderRepository.existsByEmail(testEmail)).isTrue();

        CoffeeOrder savedOrder = orderRepository.findByEmail(testEmail)
                .orElseThrow(() -> new AssertionError("주문이 저장되지 않았습니다"));
        assertThat(savedOrder.getStatements()).hasSize(1);
        assertThat(savedOrder.getStatements().get(0).getOrderItems()).hasSize(1);
        assertThat(savedOrder.getStatements().get(0).getOrderItems().get(0).getProduct().getId())
                .isEqualTo(savedProduct.getId());
    }

    @Test
    @DisplayName("✅ [통합] 다중 주문 항목 - Service 의 반복 로직 검증")
    void createOrder_success_withMultipleItems_verifyServiceLoop() throws Exception {
        // given
        Product product2 = productRepository.save(new Product("라떼", 5000, 2));

        OrderItemRequestDto item1 = new OrderItemRequestDto(0, savedProduct.getId(), 1);
        OrderItemRequestDto item2 = new OrderItemRequestDto(0, product2.getId(), 3);

        OrderStatementRequestDto statementDto = new OrderStatementRequestDto(
                0, "부산시 해운대구", "61234", new OrderItemRequestDto[]{item1, item2}
        );
        OrderCreateRequestDto requestDto = new OrderCreateRequestDto("multi@test.com", statementDto);
        String requestJson = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then - Service 의 for 문 로직이 모든 항목을 처리했는지 검증
        CoffeeOrder savedOrder = orderRepository.findByEmail("multi@test.com").orElseThrow();
        assertThat(savedOrder.getStatements().get(0).getOrderItems()).hasSize(2);
    }

    // ==================== ❌ @Valid 검증 실패 (400) - Controller 단 검증 ====================

    @Test
    @DisplayName("❌ [Controller] 이메일 형식 오류 - @Valid 검증 실패 (400)")
    void createOrder_fail_emailValidation() throws Exception {
        // given
        OrderCreateRequestDto requestDto = OrderTestDataFactory.createRequestDtoWithEmailInvalidFormat();
        String requestJson = objectMapper.writeValueAsString(requestDto);

        // when & then - Service 호출 전 Controller 에서 검증 실패
        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다"));
    }

    @Test
    @DisplayName("❌ [Controller] 우편번호 길이 오류 - @Valid 검증 실패 (400)")
    void createOrder_fail_zipCodeValidation() throws Exception {
        // given
        OrderCreateRequestDto requestDto = OrderTestDataFactory.createRequestDtoWithZipCodeInvalidLength();
        String requestJson = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("우편번호는 5 자리여야 합니다"));
    }

    // ==================== ❌ 비즈니스 로직 실패 (400) - Service 단 검증 ====================

    @Test
    @DisplayName("❌ [Service] 중복 이메일 - OrderService 비즈니스 규칙 위반 (400)")
    void createOrder_fail_duplicateEmail_verifyServiceLogic() throws Exception {
        // given
        String duplicateEmail = "duplicate@test.com";

        // 1 차 주문: 성공 (Service 로직 정상 수행)
        OrderCreateRequestDto firstRequest = OrderTestDataFactory.createValidRequestDto(duplicateEmail);
        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        // 2 차 주문: 동일 이메일 → Service 에서 RuntimeException 발생 → Controller 가 400 으로 매핑
        OrderCreateRequestDto secondRequest = OrderTestDataFactory.createValidRequestDto(duplicateEmail);
        String requestJson = objectMapper.writeValueAsString(secondRequest);

        // when & then
        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())  // ✅ RuntimeException → 400
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 주문된 이메일입니다"));  // ✅ 예외 메시지 전달
    }

    @Test
    @DisplayName("❌ [Service] 존재하지 않는 상품 - ProductService.productExists() 예외 (400)")
    void createOrder_fail_nonExistentProduct_verifyServiceLogic() throws Exception {
        // given
        // DB 에 없는 상품 ID(99999) 사용 → ProductService.productExists() 에서 RuntimeException 발생
        OrderItemRequestDto invalidItem = new OrderItemRequestDto(0, 99999, 1);
        OrderStatementRequestDto statementDto = new OrderStatementRequestDto(
                0, "대구시 중구", "41234", new OrderItemRequestDto[]{invalidItem}
        );
        OrderCreateRequestDto requestDto = new OrderCreateRequestDto("product-error@test.com", statementDto);
        String requestJson = objectMapper.writeValueAsString(requestDto);

        // when & then
        // Service 레이어 예외 → Controller catch (RuntimeException) → 400 응답
        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("상품을 찾을 수 없습니다: 99999"));
    }

    // ==================== ✅ 신규/기존 주문 분리 처리 검증 (요구사항 핵심) ====================

    @Test
    @DisplayName("✅ [Service] 신규 주문과 기존 주문 추가 분리 처리 로직 검증")
    void createOrder_verifyNewAndExistingOrderSeparation() throws Exception {
        // given - 신규 사용자
        String newEmail = "newuser@test.com";
        OrderCreateRequestDto newOrderRequest = OrderTestDataFactory.createValidRequestDto(newEmail);

        // when - 신규 주문 생성 (Service: !existsByEmail → 신규 로직)
        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newOrderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(newEmail));

        // then - DB 에 신규 주문 저장 확인
        assertThat(orderRepository.existsByEmail(newEmail)).isTrue();

        // when - 동일 이메일 재주문 시도 (Service: existsByEmail → 기존 로직/예외)
        OrderCreateRequestDto duplicateRequest = OrderTestDataFactory.createValidRequestDto(newEmail);

        // then - 비즈니스 규칙에 의해 거부됨 (분리된 로직의 결과)
        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 주문된 이메일입니다"));
    }
}