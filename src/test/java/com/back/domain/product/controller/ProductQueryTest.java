package com.back.domain.product.controller;

import com.back.domain.product.dto.query.ProductQueryResponseDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ProductQueryTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @BeforeEach
    void setUp() {
        // 테스트 전 데이터 초기화 (선택 사항)
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("✅ 제품 전체 조회 - 성공 케이스")
    class FindAllSuccess {

        @Test
        @DisplayName("등록된 상품이 있을 때 - 200 OK 및 items 리스트 반환")
        void findAll_withProducts_returnsProductList() throws Exception {
            // given
            Product product1 = productService.addProduct("아메리카노", 4500, 1);
            Product product2 = productService.addProduct("카페라떼", 5000, 2);
            Product product3 = productService.addProduct("카푸치노", 5500, 3);

            // when & then
            mockMvc.perform(get("/api/v1/product")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    // 응답 구조 검증
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items.length()").value(3))
                    // 첫 번째 아이템 검증
                    .andExpect(jsonPath("$.items[0].productId").value(product1.getId()))
                    .andExpect(jsonPath("$.items[0].name").value("아메리카노"))
                    .andExpect(jsonPath("$.items[0].price").value(4500))
                    .andExpect(jsonPath("$.items[0].imageSeq").value(1))
                    // 두 번째 아이템 검증
                    .andExpect(jsonPath("$.items[1].productId").value(product2.getId()))
                    .andExpect(jsonPath("$.items[1].name").value("카페라떼"))
                    .andExpect(jsonPath("$.items[1].price").value(5000))
                    .andExpect(jsonPath("$.items[1].imageSeq").value(2));
        }

        @Test
        @DisplayName("등록된 상품이 없을 때 - 빈 배열 반환")
        void findAll_withNoProducts_returnsEmptyList() throws Exception {
            // given
            productRepository.deleteAll();

            // when & then
            mockMvc.perform(get("/api/v1/product")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items.length()").value(0));
        }

        @Test
        @DisplayName("상품이 1 개만 있을 때 - 단일 아이템 정상 반환")
        void findAll_withSingleProduct_returnsOneItem() throws Exception {
            // given
            Product product = productService.addProduct("에스프레소", 3000, 10);

            // when & then
            mockMvc.perform(get("/api/v1/product")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.items[0].productId").value(product.getId()))
                    .andExpect(jsonPath("$.items[0].name").value("에스프레소"))
                    .andExpect(jsonPath("$.items[0].price").value(3000))
                    .andExpect(jsonPath("$.items[0].imageSeq").value(10));
        }
    }

    @Nested
    @DisplayName("📊 DB 연동 및 정렬 검증")
    class DatabaseAndOrderingTest {

        @Test
        @DisplayName("DB 에 저장된 데이터와 응답 데이터 일치 검증")
        void findAll_verifyDataConsistencyWithDB() throws Exception {
            // given
            String testName = "테스트-상품-" + System.currentTimeMillis();
            int testPrice = 9999;
            int testImageSeq = 77;

            Product saved = productService.addProduct(testName, testPrice, testImageSeq);

            // when
            String response = mockMvc.perform(get("/api/v1/product")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            ProductQueryResponseDto responseDto = objectMapper.readValue(
                    response, ProductQueryResponseDto.class);

            // then
            assertThat(responseDto.items()).isNotEmpty();
            var foundItem = responseDto.items().stream()
                    .filter(item -> item.productId() == saved.getId())
                    .findFirst()
                    .orElseThrow();

            assertThat(foundItem.name()).isEqualTo(testName);
            assertThat(foundItem.price()).isEqualTo(testPrice);
            assertThat(foundItem.imageSeq()).isEqualTo(testImageSeq);
        }

        @Test
        @DisplayName("상품 조회 시 ID 오름차순 정렬 검증")
        void findAll_verifyOrderingByIdAsc() throws Exception {
            // given - 의도적으로 ID 가 증가하는 순서대로 생성
            Product first = productService.addProduct("상품-첫번째", 1000, 1);
            Product second = productService.addProduct("상품-두번째", 2000, 2);
            Product third = productService.addProduct("상품-세번째", 3000, 3);

            // when & then
            mockMvc.perform(get("/api/v1/product")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(3))
                    // ID 오름차순 정렬 검증
                    .andExpect(jsonPath("$.items[0].productId").value(first.getId()))
                    .andExpect(jsonPath("$.items[1].productId").value(second.getId()))
                    .andExpect(jsonPath("$.items[2].productId").value(third.getId()));
        }
    }
}
