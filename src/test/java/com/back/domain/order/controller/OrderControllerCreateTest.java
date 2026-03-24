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

    // ==================== ✅ 주문 생성/명세 추가 - 성공 케이스 ====================
    @Nested
    @DisplayName("✅ 주문 생성/명세 추가 - 성공 케이스")
    class CreateOrderSuccess {

        @Test
        @DisplayName("새 이메일로 주문 - Order 와 Statement 동시 생성")
        void createOrder_newEmail_createsOrderAndStatement() throws Exception {
            String testEmail = "new-order@test.com";
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
                    .andExpect(jsonPath("$.message").value("성공"))
                    .andExpect(jsonPath("$.data.email").value(testEmail))
                    .andExpect(jsonPath("$.data.orderId").isNumber())
                    .andExpect(jsonPath("$.data.createdAt").exists());

            // DB 검증
            assertThat(orderRepository.existsByEmail(testEmail)).isTrue();
            CoffeeOrder savedOrder = orderRepository.findByEmail(testEmail)
                    .orElseThrow(() -> new AssertionError("주문이 저장되지 않았습니다"));
            assertThat(savedOrder.getStatements()).hasSize(1);
            assertThat(savedOrder.getStatements().getFirst().getOrderItems()).hasSize(1);
        }

        @Test
        @DisplayName("기존 이메일로 주문 - 기존 Order 에 Statement 추가")
        void createOrder_existingEmail_addsStatementOnly() throws Exception {
            String testEmail = "existing-order@test.com";

            // 1. 첫 주문 생성
            OrderCreateRequestDto firstRequest = OrderTestDataFactory.createValidRequestDto(
                    testEmail,
                    savedProduct.getId()
            );
            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isOk());

            // 2. 동일 이메일로 명세 추가 (다른 주소)
            OrderCreateRequestDto secondRequest = OrderTestDataFactory.builder()
                    .email(testEmail)
                    .address("부산시 해운대구")
                    .zipCode("67890")
                    .clearItems()
                    .addItem(savedProduct.getId(), 3)
                    .build();

            String requestJson = objectMapper.writeValueAsString(secondRequest);

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("성공"))
                    .andExpect(jsonPath("$.data.email").value(testEmail));

            // DB 검증: 명세가 2 개 생성되었는지
            CoffeeOrder savedOrder = orderRepository.findByEmail(testEmail).orElseThrow();
            assertThat(savedOrder.getStatements()).hasSize(2);
            assertThat(savedOrder.getStatements().get(1).getOrderItems()).hasSize(1);
            assertThat(savedOrder.getStatements().get(1).getOrderItems().getFirst().getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("동일 이메일 + 동일 주소 - 새 Statement 생성 (비즈니스 규칙)")
        void createOrder_sameEmailSameAddress_createsNewStatement() throws Exception {
            String testEmail = "same-address@test.com";
            String sameAddress = "서울시 강남구";
            String zipCode = "12345";

            // 1 번째 주문
            OrderCreateRequestDto firstRequest = OrderTestDataFactory.builder()
                    .email(testEmail)
                    .address(sameAddress)
                    .zipCode(zipCode)
                    .addItem(savedProduct.getId(), 1)
                    .build();

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isOk());

            // 2 번째 주문 (동일 주소)
            OrderCreateRequestDto secondRequest = OrderTestDataFactory.builder()
                    .email(testEmail)
                    .address(sameAddress)
                    .zipCode(zipCode)
                    .addItem(savedProduct.getId(), 2)
                    .build();

            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondRequest)))
                    .andExpect(status().isOk());

            // DB 검증: 명세가 2 개 생성되었는지 (비즈니스 규칙에 따라 다름)
            CoffeeOrder savedOrder = orderRepository.findByEmail(testEmail).orElseThrow();
            assertThat(savedOrder.getStatements()).hasSize(2);  // 새 명세 생성 정책
        }

        @Test
        @DisplayName("다중 주문 항목 - Service 반복 로직 검증")
        void createOrder_success_withMultipleItems() throws Exception {
            Product product2 = productRepository.save(new Product("카페라떼", 5000, 2));
            Product product3 = productRepository.save(new Product("카푸치노", 5500, 3));

            OrderCreateRequestDto requestDto = OrderTestDataFactory.builder()
                    .email("multi-item@test.com")
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
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("성공"));

            CoffeeOrder savedOrder = orderRepository.findByEmail("multi-item@test.com").orElseThrow();
            assertThat(savedOrder.getStatements().getFirst().getOrderItems()).hasSize(3);
        }
    }

    // ==================== ❌ 주문 생성 - 실패 케이스 ====================
    @Nested
    @DisplayName("❌ 주문 생성 - 실패 케이스")
    class CreateOrderFail {

        @Test
        @DisplayName("존재하지 않는 상품 - 400 Bad Request")
        void createOrder_fail_nonExistentProduct() throws Exception {
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
    }

    // ==================== ❌ @Valid 검증 실패 ====================
    @Nested
    @DisplayName("❌ @Valid 검증 실패 - Controller 단 검증")
    class ValidationFail {

        @Test
        @DisplayName("이메일 형식 오류 - 400 Bad Request")
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
        @DisplayName("우편번호 길이 오류 - 400 Bad Request")
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
        @DisplayName("수량 0 - 400 Bad Request")
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
    }

    // ==================== 📊 DB 연동 검증 ====================
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

            // 테스트 내에서는 증가 확인 (롤백되므로 실제 DB 는 변하지 않음)
            assertThat(orderRepository.count()).isEqualTo(beforeCount + 1);
        }

        @Test
        @DisplayName("명세 삭제 후 주문 자동 삭제 검증 (orphanRemoval)")
        void removeStatement_verifyOrderAutoDelete() throws Exception {
            String testEmail = "delete-test@test.com";

            OrderCreateRequestDto createRequest = OrderTestDataFactory.createValidRequestDto(
                    testEmail,
                    savedProduct.getId()
            );
            mockMvc.perform(post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk());

            CoffeeOrder savedOrder = orderRepository.findByEmail(testEmail).orElseThrow();
            int orderId = savedOrder.getId();
            int statementId = savedOrder.getStatements().getFirst().getId();

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .delete("/api/v1/order/{orderId}/statement/{orderStatementId}", orderId, statementId))
                    .andExpect(status().isNoContent());

            // orphanRemoval + @OneToMany 에서 마지막 명세 삭제 시 주문도 삭제됨
            assertThat(orderRepository.findById(orderId)).isEmpty();
        }
    }
}