package com.back.order.controller;

import com.back.domain.order.entity.Order;
import com.back.domain.order.entity.OrderItem;
import com.back.domain.order.entity.OrderStatement;
import com.back.domain.order.service.OrderService;
import com.back.domain.product.entity.Product;
import com.back.domain.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class OrderControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @BeforeEach
    void setUp() {
        // 테스트용 상품 데이터 생성
        Product product1 = productService.addProduct("상품 1", 10000, 1);
        Product product2 = productService.addProduct("상품 2", 20000, 2);
        Product product3 = productService.addProduct("상품 3", 30000, 3);

        // 테스트용 주문 데이터 생성
        Order order1 = orderService.createOrder("tset01@example.com");
        Order order2 = orderService.createOrder("tset02@example.com");
        Order order3 = orderService.createOrder("tset03@example.com");

        //테스트용 배송 정보 등록
        OrderStatement orderStatement1 = orderService.setDeliveryInfo("경기도 군포시", "1",order1);
        OrderStatement orderStatement2 = orderService.setDeliveryInfo("서울시 강남구", "2",order2);
        OrderStatement orderStatement3 = orderService.setDeliveryInfo("서울시 도봉구", "3",order3);

        //테스트용 주문 항목 추가
        OrderItem orderItem1 = orderService.addMenu(orderStatement1, product1, 1);
        OrderItem orderItem2 = orderService.addMenu(orderStatement2,product2,2);
        OrderItem orderItem3 = orderService.addMenu(orderStatement3,product3,3);

    }

    @Test
    @DisplayName("신규 주문 생성 성공 - 201 Created")
    void t1_createNewOrder_Success() throws Exception {
        String email = "coffeebean23@gmail.com";
    }

    @Test
    @DisplayName("기존 주문에 항목 추가 성공 - 201 Created")
    void t2_addToExistingOrder_Success() throws Exception {

    }

    @Test
    @DisplayName("이메일 누락 시 400 Bad Request")
    void t3_createOrder_Fail_EmptyEmail() throws Exception {

    }

    @Test
    @DisplayName("존재하지 않는 상품 ID 로 주문 시 400 Bad Request")
    void t4_createOrder_Fail_ProductNotFound() throws Exception {
    }

    @Test
    @DisplayName("주문수량이 0 이하일 때 400 Bad Request")
    void t5_createOrder_Fail_InvalidQuantity() throws Exception {

    }

    @Test
    @DisplayName("OrderStatements 가 null 일 때 400 Bad Request")
    void t6_createOrder_Fail_NullOrderStatements() throws Exception {
    }


    @Test
    @DisplayName("OrderStatements 가 빈 배열일 때 400 Bad Request")
    void t7_createOrder_Fail_EmptyOrderStatements() throws Exception {
    }

    @Test
    @DisplayName("배송 주소 누락 시 400 Bad Request")
    void t8_createOrder_Fail_EmptyAddress() throws Exception {
    }

    @Test
    @DisplayName("중복 이메일로 주문 시 500 Internal Server Error")
    void t9_createOrder_Fail_DuplicateEmail() throws Exception {
    }

    @Test
    @DisplayName("여러 상품을 포함한 주문 생성 성공")
    void t10_createOrder_MultipleProducts_Success() throws Exception {}

    @Test
    @DisplayName("여러 배송지를 포함한 주문 생성 성공")
    void t11_createOrder_MultipleStatements_Success() throws Exception {}

    }
