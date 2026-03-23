package com.back;

import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1OrderControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 다건 조회")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/api/v1/order"))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("주문 단건 조회 (1번 글) - 성공")
    void t2() throws Exception {
        int targetId = 1;

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
    @DisplayName("주문 단건 조회 - 실패") // 예상 404에러 나올 것
    void t3() throws Exception {
        int targetId = Integer.MAX_VALUE; // 존재하지 않은 order아이디 넘겨주기

        ResultActions resultActions = mvc
                .perform(get("/api/v1/order/%d".formatted(targetId)))
                .andDo(print());

        resultActions
                .andExpect(status().is4xxClientError());
    }
}