package com.back.domain.order.controller;

import com.back.domain.order.dto.create.OrderCreateRequestDto;
import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.factory.OrderTestDataFactory;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.repository.ProductRepository;
import com.back.domain.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Autowired
    private ProductService productService;

    private Product savedProduct;

    @BeforeEach
    void setUp() {
        savedProduct = productService.addProduct("아메리카노", 4500, 1);
    }

    @Nested
    @DisplayName("✅ 주문 생성 - 성공 케이스")
    class CreateOrderSuccess {

        @Test
        @DisplayName("주문 생성 성공 - Service 비즈니스 로직 정상 수행")
        void createOrder_success_verifyServiceFlow() throws Exception {

            String testEmail = "integration@test.com";

            OrderCreateRequestDto requestDto = OrderTestDataFactory.createValidRequestDto(
                    testEmail,
                    savedProduct.getId()
            );

            String requestJson = objectMapper.writeValueAsString(requestDto);

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value(testEmail))
                    .andExpect(jsonPath("$.data.orderId").exists())
                    .andReturn();

            assertThat(orderRepository.existsByEmail(testEmail)).isTrue();

            CoffeeOrder savedOrder = orderRepository.findByEmail(testEmail)
                    .orElseThrow(() -> new AssertionError("주문이 저장되지 않았습니다"));
            assertThat(savedOrder.getStatements()).hasSize(1);
            assertThat(savedOrder.getStatements().get(0).getOrderItems()).hasSize(1);
        }

        @Test
        @DisplayName("다중 주문 항목 - Service 반복 로직 검증")
        void createOrder_success_withMultipleItems_verifyServiceLoop() throws Exception {
            Product product2 = productRepository.save(new Product("카페라떼", 5000, 2));
            Product product3 = productRepository.save(new Product("카푸치노", 5500, 3));

            OrderCreateRequestDto requestDto = OrderTestDataFactory.builder()
                    .email("multi@test.com")
                    .clearItems()
                    .addDefaultItem(savedProduct.getId())
                    .addDefaultItem(product2.getId())
                    .addDefaultItem(product3.getId())
                    .build();

            String requestJson = objectMapper.writeValueAsString(requestDto);

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Service 의 for 문 로직이 모든 항목을 처리했는지 검증
            CoffeeOrder savedOrder = orderRepository.findByEmail("multi@test.com").orElseThrow();
            assertThat(savedOrder.getStatements().get(0).getOrderItems()).hasSize(3);
        }

        @Test
        @DisplayName("Builder 패턴으로 복잡한 주문 생성")
        void createOrder_success_withBuilder() throws Exception {

            OrderCreateRequestDto requestDto = OrderTestDataFactory.builder()
                    .email("builder@test.com")
                    .address("부산시 해운대구")
                    .zipCode("67890")
                    .clearItems()
                    .addItem(savedProduct.getId(), 2)
                    .addItem(savedProduct.getId(), 3)
                    .build();

            String requestJson = objectMapper.writeValueAsString(requestDto);

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("builder@test.com"));

            // DB 검증
            CoffeeOrder savedOrder = orderRepository.findByEmail("builder@test.com").orElseThrow();
            assertThat(savedOrder.getStatements().get(0).getAddress()).isEqualTo("부산시 해운대구");
            assertThat(savedOrder.getStatements().get(0).getOrderItems()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("❌ 비즈니스 로직 실패 - Service 단 검증")
    class CreateOrderFailBusinessLogic {

        @Test
        @DisplayName("존재하지 않는 상품 - ProductService.productExists() 예외 (400)")
        void createOrder_fail_nonExistentProduct_verifyServiceLogic() throws Exception {

            OrderCreateRequestDto requestDto = OrderTestDataFactory.builder()
                    .email("product-error@test.com")
                    .clearItems()
                    .addDefaultItem(99999)
                    .build();

            String requestJson = objectMapper.writeValueAsString(requestDto);

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("상품을 찾을 수 없습니다: 99999"));
        }

        @Test
        @DisplayName("중복 이메일 - OrderService 비즈니스 규칙 위반 (400)")
        void createOrder_fail_duplicateEmail_verifyServiceLogic() throws Exception {
            String duplicateEmail = "duplicate@test.com";


            OrderCreateRequestDto firstRequest = OrderTestDataFactory.createValidRequestDto(
                    duplicateEmail,
                    savedProduct.getId()
            );
            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isOk());

            OrderCreateRequestDto secondRequest = OrderTestDataFactory.createValidRequestDto(
                    duplicateEmail,
                    savedProduct.getId()
            );
            String requestJson = objectMapper.writeValueAsString(secondRequest);

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("이미 주문된 이메일입니다"));
        }
    }

    @Nested
    @DisplayName("❌ @Valid 검증 실패 - Controller 단 검증")
    class CreateOrderFailValidation {

        @Test
        @DisplayName("이메일 형식 오류 - @Valid 검증 실패 (400)")
        void createOrder_fail_emailInvalidFormat() throws Exception {

            OrderCreateRequestDto requestDto = OrderTestDataFactory
                    .createRequestDtoWithEmailInvalidFormat(savedProduct.getId());
            String requestJson = objectMapper.writeValueAsString(requestDto);

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다"));
        }

        @Test
        @DisplayName("우편번호 길이 오류 - @Valid 검증 실패 (400)")
        void createOrder_fail_zipCodeInvalidLength() throws Exception {

            OrderCreateRequestDto requestDto = OrderTestDataFactory
                    .createRequestDtoWithZipCodeTooShort(savedProduct.getId());
            String requestJson = objectMapper.writeValueAsString(requestDto);

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("우편번호는 5자리여야 합니다"));
        }

        @Test
        @DisplayName("상품 ID 0 - @Min(1) 검증 실패 (400)")
        void createOrder_fail_productIdZero() throws Exception {

            OrderCreateRequestDto requestDto = OrderTestDataFactory
                    .createRequestDtoWithProductIdZero();
            String requestJson = objectMapper.writeValueAsString(requestDto);

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("상품은 1개 이상이어야 합니다"));
        }

        @Test
        @DisplayName("상품 ID 음수 - @Min(1) 검증 실패 (400)")
        void createOrder_fail_productIdNegative() throws Exception {

            OrderCreateRequestDto requestDto = OrderTestDataFactory
                    .createRequestDtoWithProductIdNegative();
            String requestJson = objectMapper.writeValueAsString(requestDto);

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("상품은 1개 이상이어야 합니다"));
        }

        @Test
        @DisplayName("수량 0 - @Min(1) 검증 실패 (400)")
        void createOrder_fail_quantityZero() throws Exception {

            OrderCreateRequestDto requestDto = OrderTestDataFactory
                    .createRequestDtoWithQuantityZero(savedProduct.getId());
            String requestJson = objectMapper.writeValueAsString(requestDto);

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("수량은 1개 이상이어야 합니다"));
        }

        @Test
        @DisplayName("수량 음수 - @Min(1) 검증 실패 (400)")
        void createOrder_fail_quantityNegative() throws Exception {

            OrderCreateRequestDto requestDto = OrderTestDataFactory
                    .createRequestDtoWithQuantityNegative(savedProduct.getId());
            String requestJson = objectMapper.writeValueAsString(requestDto);

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("수량은 1개 이상이어야 합니다"));
        }
    }

    @Nested
    @DisplayName("📊 DB 연동 검증")
    class DatabaseIntegrationTest {

        @Test
        @DisplayName("테스트 간 데이터 격리 (@Transactional)")
        void testIsolation_withTransactional() throws Exception {

            long beforeCount = orderRepository.count();

            OrderCreateRequestDto request = OrderTestDataFactory.createValidRequestDto(
                    "iso@test.com",
                    savedProduct.getId()
            );
            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // 테스트 내에서는 증가 확인
            assertThat(orderRepository.count()).isEqualTo(beforeCount + 1);
        }
    }
}
