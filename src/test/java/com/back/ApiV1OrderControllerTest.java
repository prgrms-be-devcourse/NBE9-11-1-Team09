package com.back;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.back.domain.order.controller.OrderController;
import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.entity.OrderStatement;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.order.repository.OrderStatementRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.service.ProductService;
import jakarta.persistence.EntityManager;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1OrderControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private EntityManager em;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @BeforeEach
    void setUp() {
        Product p1 = productService.addProduct("커피콩1", 5000, 1);
        Product p2 = productService.addProduct("커피콩2", 6000, 2);
        Product p3 = productService.addProduct("커피콩3", 7000, 3);
        Product p4 = productService.addProduct("커피콩4", 8000, 4);

        // 2. 테스트용 주문 데이터 생성
        CoffeeOrder order1 = new CoffeeOrder("test@test.com");
        OrderStatement os1 = order1.addOrderStatement("서울", "12345");
        os1.addOrderItem(2, p1);
        os1.addOrderItem(1, p2);

        CoffeeOrder order2 = new CoffeeOrder("user@test.com");
        order2.addOrderStatement("부산", "54321");

        // 3. 저장 (Rollback 되므로 중복 저장 걱정 없음)
        orderRepository.save(order1);
        orderRepository.save(order2);
    }

    @Test
    @DisplayName("주문 다건 조회")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/api/v1/order"))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())

                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].email").isString())
                .andExpect(jsonPath("$[0].orderStatements").isArray())

                // 3. 중첩 객체 검증 (orderStatements[0])
                .andExpect(jsonPath("$[0].orderStatements[0].address").isString())
                .andExpect(jsonPath("$[0].orderStatements[0].zipCode").isString())
                .andExpect(jsonPath("$[0].orderStatements[0].orderItems").isArray())

                // 4. 더 깊은 중첩 검증 (orderItems[0].productItem)
                .andExpect(jsonPath("$[0].orderStatements[0].orderItems[0].productItem.name").isString())
                .andExpect(jsonPath("$[0].orderStatements[0].orderItems[0].quantity").isNumber())

                // 5. 비즈니스 규칙 검증 (예: 수량은 1 이상)
                .andExpect(jsonPath("$[0].orderStatements[0].orderItems[0].quantity").value(
                        Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("주문 단건 조회 (1번 글) - 성공")
    void t2() throws Exception {
        CoffeeOrder savedOrder = orderRepository.findAll().stream()
                .filter(o -> o.getEmail().equals("test@test.com"))
                .findFirst()
                .orElseThrow();
        int targetId = savedOrder.getId();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/order/%d".formatted(targetId))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk()) // 상태 코드가 200인지 확인
                .andExpect(jsonPath("$.email").value("test@test.com"));
        //우리는 이미 1번의 이메일이 test@test.com임을 알고 있다. (BaseInitData에서 정의함)

    }

    @Test
    @DisplayName("주문 단건 조회 - 구조 및 타입 검증")
    void t3() throws Exception {
        CoffeeOrder savedOrder = orderRepository.findAll().stream()
                .filter(o -> o.getEmail().equals("test@test.com"))
                .findFirst()
                .orElseThrow();
        int targetId = savedOrder.getId();

        mvc.perform(get("/api/v1/order/{id}", targetId))
                .andExpect(status().isOk())

                // 1. 루트 레벨: 객체인지 확인 (배열 아님)
                .andExpect(jsonPath("$").isMap())

                // 2. 필수 필드 존재 여부 및 타입 검증
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.email").isString())
                .andExpect(jsonPath("$.orderStatements").isArray()) // 컬렉션 필드는 배열로 응답

                // 3. 중첩 객체 검증 (orderStatements[0])
                .andExpect(jsonPath("$.orderStatements[0].id").isNumber())
                .andExpect(jsonPath("$.orderStatements[0].address").isString())
                .andExpect(jsonPath("$.orderStatements[0].zipCode").isString())
                .andExpect(jsonPath("$.orderStatements[0].orderItems").isArray())

                // 4. 더 깊은 중첩 검증 (orderItems[0].productItem)
                .andExpect(jsonPath("$.orderStatements[0].orderItems[0].id").isNumber())
                .andExpect(jsonPath("$.orderStatements[0].orderItems[0].quantity").isNumber())
                .andExpect(jsonPath("$.orderStatements[0].orderItems[0].productItem.productId").isNumber())
                .andExpect(jsonPath("$.orderStatements[0].orderItems[0].productItem.name").isString())
                .andExpect(jsonPath("$.orderStatements[0].orderItems[0].productItem.price").isNumber())

                // 5. 비즈니스 규칙 검증 (예: 수량은 1 이상)
                .andExpect(jsonPath("$.orderStatements[0].orderItems[0].quantity").value(
                        Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("주문 단건 조회 - 실패")
        // 예상 404에러 나올 것
    void t4() throws Exception {
        int targetId = Integer.MAX_VALUE; // 존재하지 않은 order아이디 넘겨주기

        ResultActions resultActions = mvc
                .perform(get("/api/v1/order/%d".formatted(targetId)))
                .andDo(print());

        resultActions
                .andExpect(status().is4xxClientError());
    }


    // 정상적 입력에 대해 호출된 핸들러 & 응답 코드 확인 + db 정상 삭제 확인
    @Test
    @DisplayName("정상적인 삭제 요청에 대한 테스트")
    void delete_t1() throws Exception {
        //given
        CoffeeOrder order = orderRepository.findAll().stream()
                .filter(o -> o.getEmail().equals("test@test.com"))
                .findFirst()
                .orElseThrow();
        int orderId = order.getId();

        OrderStatement statement = orderStatementRepository.findAll().stream()
                .filter(o -> o.getAddress().equals("서울"))
                .findFirst()
                .orElseThrow();
        int statementId = statement.getId();

        //when
        ResultActions res = mvc.perform(
                delete("/api/v1/order/%d/statement/%d".formatted(orderId, statementId))
        ).andDo(print());

        em.flush();
        em.clear();

        //then
        res.andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("removeOrderStatement"))
                .andExpect(status().isNoContent());

        // order 삭제 확인
        assertThat(orderRepository.findById(orderId)).isEmpty();
        // statement 삭제 확인
        assertThat(orderStatementRepository.findById(statementId)).isEmpty();
    }

    // 오류에 대한 예외 처리 확인 ( 예외 종류 , 메세지 )
    @Test
    @DisplayName("존재하지않는 Order Id에 대한 예외 처리 테스트")
    void delete_t2() throws Exception {

        int orderId = Integer.MAX_VALUE;

        OrderStatement statement = orderStatementRepository.findAll().stream()
                .filter(o -> o.getAddress().equals("서울"))
                .findFirst()
                .orElseThrow();
        int statementId = statement.getId();

        //when
        ResultActions res = mvc.perform(
                delete("/api/v1/order/%d/statement/%d".formatted(orderId, statementId))
        ).andDo(print());

        em.flush();
        em.clear();

        //then
        res.andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("removeOrderStatement"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("존재하지않는 Order_Id입니다.")
                );
    }

    @Test
    @DisplayName("존재하지않는 OrderStatement Id에 대한 예외 처리 테스트")
    void delete_t3() throws Exception {

        CoffeeOrder order = orderRepository.findAll().stream()
                .filter(o -> o.getEmail().equals("test@test.com"))
                .findFirst()
                .orElseThrow();
        int orderId = order.getId();

        int statementId = Integer.MAX_VALUE;

        //when
        ResultActions res = mvc.perform(
                delete("/api/v1/order/%d/statement/%d".formatted(orderId, statementId))
        ).andDo(print());

        em.flush();
        em.clear();

        //then
        res.andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("removeOrderStatement"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("존재하지않는 OrderStatement_Id입니다.")
                );
    }

    @Autowired
    private OrderStatementRepository orderStatementRepository;
}