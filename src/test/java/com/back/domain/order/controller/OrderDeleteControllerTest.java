package com.back.domain.order.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.entity.OrderStatement;
import com.back.domain.order.repository.OrderItemRepository;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.order.repository.OrderStatementRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
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
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class OrderDeleteControllerTest {

    private int EXIST_ID;
    private int NOT_EXIST_ID;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    EntityManager em;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderStatementRepository orderStatementRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;


    @BeforeEach
    void setUp() {
        for (int i = 1; i <= 4; i++) {
            Product product = productRepository.findById(i).get();
            CoffeeOrder order = new CoffeeOrder("test %d@naver.com".formatted(i));
            OrderStatement statement = order.addOrderStatement("address" + i, "zipCord" + i);
            statement.addOrderItem(i, product);

            orderRepository.save(order);
            if (i == 1) {
                EXIST_ID = order.getId();
            }
            if (i == 4) {
                NOT_EXIST_ID = order.getId() + 1;
            }
        }

        em.flush();
        em.clear();
    }

    // 정상적 입력에 대해 호출된 핸들러 & 응답 코드 확인 + db 정상 삭제 확인
    @Test
    @DisplayName("정상적인 삭제 요청에 대한 테스트")
    void delete_t1() throws Exception {
        //given
        int orderId = EXIST_ID;
        int statementId = EXIST_ID;
        int itemId = EXIST_ID;

        //when
        ResultActions res = mockMvc.perform(
                delete("/api/v1/order/%d/statement/%d".formatted(orderId, statementId))
        );

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
        // orderItem 삭제 확인 (id=1이라고 가정)
        assertThat(orderItemRepository.findById(itemId)).isEmpty();
    }

    // 오류에 대한 예외 처리 확인 ( 예외 종류 , 메세지 )
    @Test
    @DisplayName("존재하지않는 Order Id에 대한 예외 처리 테스트")
    void delete_t2() throws Exception {

        //when
        ResultActions res = mockMvc.perform(
                delete("/api/v1/order/%d/statement/%d".formatted(NOT_EXIST_ID, EXIST_ID))
        );

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
}