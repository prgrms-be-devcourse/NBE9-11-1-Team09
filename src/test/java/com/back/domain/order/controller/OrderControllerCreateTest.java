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

    // ==================== ✅ 신규 주문 생성 - 성공 케이스 ====================
    @Nested
    @DisplayName("✅ 신규 주문 생성 - 성공 케이스")
    class CreateNewOrderSuccess {

        @Test
        @DisplayName("신규 주문 생성 성공 - Service 비즈니스 로직 정상 수행")
        void createNewOrder_success_verifyServiceFlow() throws Exception {
            String testEmail = "new-order@test.com";
            OrderCreateRequestDto requestDto = OrderTestDataFactory.createValidRequestDto(
                    testEmail,
                    savedProduct.getId()
            );
            String requestJson = objectMapper.writeValueAsString(requestDto);

            mockMvc.perform(post("/api/v1/order/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value(testEmail))
                    .andExpect(jsonPath("$.data.orderId").exists());

            // DB 검증
            assertThat(orderRepository.existsByEmail(testEmail)).isTrue();
            CoffeeOrder savedOrder = orderRepository.findByEmail(testEmail)
                    .orElseThrow(() -> new AssertionError("주문이 저장되지 않았습니다"));
            assertThat(savedOrder.getStatements()).hasSize(1);
            assertThat(savedOrder.getStatements().get(0).getOrderItems()).hasSize(1);
        }

        @Test
        @DisplayName("다중 주문 항목 - Service 반복 로직 검증")
        void createNewOrder_success_withMultipleItems() throws Exception {
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

            mockMvc.perform(post("/api/v1/order/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Service 의 for 문 로직이 모든 항목을 처리했는지 검증
            CoffeeOrder savedOrder = orderRepository.findByEmail("multi-item@test.com").orElseThrow();
            assertThat(savedOrder.getStatements().get(0).getOrderItems()).hasSize(3);
        }
    }

    // ==================== ✅ 기존 주문에 명세 추가 - 성공 케이스 ====================
    @Nested
    @DisplayName("✅ 기존 주문에 명세 추가 - 성공 케이스")
    class AddStatementToExistingOrderSuccess {

        @Test
        @DisplayName("기존 주문에 명세 추가 성공")
        void addStatement_success_verifyServiceFlow() throws Exception {
            // 1. 먼저 신규 주문 생성
            String testEmail = "existing-order@test.com";
            OrderCreateRequestDto firstRequest = OrderTestDataFactory.createValidRequestDto(
                    testEmail,
                    savedProduct.getId()
            );
            mockMvc.perform(post("/api/v1/order/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isOk());

            OrderCreateRequestDto secondRequest = OrderTestDataFactory.builder()
                    .email(testEmail)
                    .address("부산시 해운대구")
                    .zipCode("67890")
                    .clearItems()
                    .addItem(savedProduct.getId(), 3)
                    .build();

            String requestJson = objectMapper.writeValueAsString(secondRequest);

            mockMvc.perform(post("/api/v1/order/statement")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.address").value("부산시 해운대구"))
                    .andExpect(jsonPath("$.data.zipCode").value("67890"));


            CoffeeOrder savedOrder = orderRepository.findByEmail(testEmail).orElseThrow();
            assertThat(savedOrder.getStatements()).hasSize(2); // 명세 2 개
            assertThat(savedOrder.getStatements().get(1).getOrderItems()).hasSize(1);
            assertThat(savedOrder.getStatements().get(1).getOrderItems().get(0).getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("기존 주문에 여러 명세 추가")
        void addStatement_success_multipleStatements() throws Exception {
            String testEmail = "multi-statement@test.com";

            OrderCreateRequestDto firstRequest = OrderTestDataFactory.createValidRequestDto(
                    testEmail,
                    savedProduct.getId()
            );
            mockMvc.perform(post("/api/v1/order/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isOk());

            for (int i = 0; i < 2; i++) {
                OrderCreateRequestDto addRequest = OrderTestDataFactory.builder()
                        .email(testEmail)
                        .address("주소 " + i)
                        .zipCode("12345")
                        .clearItems()
                        .addItem(savedProduct.getId(), 1)
                        .build();

                mockMvc.perform(post("/api/v1/order/statement")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(addRequest)))
                        .andExpect(status().isOk());
            }

            // DB 검증
            CoffeeOrder savedOrder = orderRepository.findByEmail(testEmail).orElseThrow();
            assertThat(savedOrder.getStatements()).hasSize(3); // 초기 1 + 추가 2
        }
    }

    // ==================== ❌ 신규 주문 생성 - 실패 케이스 ====================
    @Nested
    @DisplayName("❌ 신규 주문 생성 - 실패 케이스")
    class CreateNewOrderFail {

        @Test
        @DisplayName("중복 이메일 - 400 Bad Request")
        void createNewOrder_fail_duplicateEmail() throws Exception {
            String duplicateEmail = "duplicate@test.com";

            OrderCreateRequestDto firstRequest = OrderTestDataFactory.createValidRequestDto(
                    duplicateEmail,
                    savedProduct.getId()
            );
            mockMvc.perform(post("/api/v1/order/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isOk());

            OrderCreateRequestDto secondRequest = OrderTestDataFactory.createValidRequestDto(
                    duplicateEmail,
                    savedProduct.getId()
            );
            mockMvc.perform(post("/api/v1/order/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("이미 주문된 이메일입니다"));
        }

        @Test
        @DisplayName("존재하지 않는 상품 - 400 Bad Request")
        void createNewOrder_fail_nonExistentProduct() throws Exception {
            OrderCreateRequestDto requestDto = OrderTestDataFactory.builder()
                    .email("product-error@test.com")
                    .clearItems()
                    .addDefaultItem(99999)
                    .build();

            String requestJson = objectMapper.writeValueAsString(requestDto);

            mockMvc.perform(post("/api/v1/order/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("상품을 찾을 수 없습니다: 99999"));
        }
    }

    // ==================== ❌ 기존 주문 명세 추가 - 실패 케이스 ====================
    @Nested
    @DisplayName("❌ 기존 주문 명세 추가 - 실패 케이스")
    class AddStatementFail {

        @Test
        @DisplayName("존재하지 않는 이메일 - 400 Bad Request")
        void addStatement_fail_nonExistentEmail() throws Exception {
            OrderCreateRequestDto requestDto = OrderTestDataFactory.createValidRequestDto(
                    "nonexistent@test.com",
                    savedProduct.getId()
            );

            String requestJson = objectMapper.writeValueAsString(requestDto);

            mockMvc.perform(post("/api/v1/order/statement")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("주문을 찾을 수 없습니다: nonexistent@test.com"));
        }

        @Test
        @DisplayName("존재하지 않는 상품 - 400 Bad Request")
        void addStatement_fail_nonExistentProduct() throws Exception {
            String testEmail = "statement-product-error@test.com";

            OrderCreateRequestDto firstRequest = OrderTestDataFactory.createValidRequestDto(
                    testEmail,
                    savedProduct.getId()
            );
            mockMvc.perform(post("/api/v1/order/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isOk());

            OrderCreateRequestDto secondRequest = OrderTestDataFactory.builder()
                    .email(testEmail)
                    .clearItems()
                    .addDefaultItem(99999)
                    .build();

            mockMvc.perform(post("/api/v1/order/statement")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondRequest)))
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

            mockMvc.perform(post("/api/v1/order/new")
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

            mockMvc.perform(post("/api/v1/order/new")
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

            mockMvc.perform(post("/api/v1/order/new")
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

            mockMvc.perform(post("/api/v1/order/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // 테스트 내에서는 증가 확인
            assertThat(orderRepository.count()).isEqualTo(beforeCount + 1);
        }

        @Test
        @DisplayName("명세 삭제 후 주문 자동 삭제 검증")
        void removeStatement_verifyOrderAutoDelete() throws Exception {
            String testEmail = "delete-test@test.com";

            OrderCreateRequestDto createRequest = OrderTestDataFactory.createValidRequestDto(
                    testEmail,
                    savedProduct.getId()
            );
            mockMvc.perform(post("/api/v1/order/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk());

            CoffeeOrder savedOrder = orderRepository.findByEmail(testEmail).orElseThrow();
            int orderId = savedOrder.getId();
            int statementId = savedOrder.getStatements().get(0).getId();

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .delete("/api/v1/order/{orderId}/statement/{orderStatementId}", orderId, statementId))
                    .andExpect(status().isNoContent());

            assertThat(orderRepository.findById(orderId)).isEmpty();
        }
    }
}