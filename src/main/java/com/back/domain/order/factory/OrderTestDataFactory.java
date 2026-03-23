package com.back.domain.order.factory;


import com.back.domain.order.dto.common.orderitem.OrderItemRequestDto;
import com.back.domain.order.dto.common.orderitem.OrderItemResponseDto;
import com.back.domain.order.dto.common.orderstatement.OrderStatementRequestDto;
import com.back.domain.order.dto.common.orderstatement.OrderStatementResponseDto;
import com.back.domain.order.dto.create.OrderCreateRequestDto;
import com.back.domain.order.dto.create.OrderCreateResponseDto;
import com.back.domain.order.dto.query.OrderQueryResponseDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 주문 컨트롤러 테스트용 데이터 생성 유틸리티
 * - @Valid 검증 조건을 고려하여 유효/무효 데이터 제공
 * - 상수 정의로 Magic Number 제거
 * - Builder 패턴으로 복잡한 테스트 데이터 생성 지원
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderTestDataFactory {

    // ==================== 테스트 데이터 상수 ====================

    public static final String VALID_EMAIL = "test@email.com";
    public static final String VALID_ADDRESS = "서울시 강남구 테헤란로 123";
    public static final String VALID_ZIP_CODE = "12345";
    public static final int DEFAULT_QUANTITY = 2;
    public static final int DEFAULT_PRODUCT_ID = 1;

    public static final int MIN_PRODUCT_ID = 1;
    public static final int MAX_PRODUCT_ID = Integer.MAX_VALUE;
    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 999;
    public static final String ZIP_CODE_TOO_SHORT = "1234";
    public static final String ZIP_CODE_TOO_LONG = "123456";

    // ==================== 검증 성공 데이터 (Request) ====================

    public static OrderCreateRequestDto createValidRequestDto() {
        return createValidRequestDto(VALID_EMAIL);
    }

    public static OrderCreateRequestDto createValidRequestDto(String email) {
        return new OrderCreateRequestDto(email, createValidOrderStatementRequestDto());
    }

    public static OrderCreateRequestDto createValidRequestDto(String email, int productId) {
        return new OrderCreateRequestDto(
                email,
                createValidOrderStatementRequestDto(productId)
        );
    }

    public static OrderCreateRequestDto createValidRequestDtoWithMultipleItems(int itemCount) {
        return new OrderCreateRequestDto(
                VALID_EMAIL,
                createValidOrderStatementRequestDto(createValidOrderItems(itemCount))
        );
    }

    public static OrderStatementRequestDto createValidOrderStatementRequestDto() {
        return createValidOrderStatementRequestDto(DEFAULT_PRODUCT_ID);
    }

    public static OrderStatementRequestDto createValidOrderStatementRequestDto(int productId) {
        return new OrderStatementRequestDto(
                0, VALID_ADDRESS, VALID_ZIP_CODE,
                new OrderItemRequestDto[]{createValidOrderItemRequestDto(productId)}
        );
    }

    public static OrderStatementRequestDto createValidOrderStatementRequestDto(
            OrderItemRequestDto... orderItems) {
        return new OrderStatementRequestDto(0, VALID_ADDRESS, VALID_ZIP_CODE, orderItems);
    }

    public static OrderStatementRequestDto createValidOrderStatementRequestDto(
            String address, String zipCode, OrderItemRequestDto... orderItems) {
        return new OrderStatementRequestDto(0, address, zipCode, orderItems);
    }

    public static OrderItemRequestDto createValidOrderItemRequestDto() {
        return createValidOrderItemRequestDto(DEFAULT_PRODUCT_ID, DEFAULT_QUANTITY);
    }

    public static OrderItemRequestDto createValidOrderItemRequestDto(int productId, int quantity) {
        // ✅ id=0 은 생성 시 DB 에서 자동 생성되는 값 (테스트용 임시값)
        return new OrderItemRequestDto(0, productId, quantity);
    }

    public static OrderItemRequestDto createValidOrderItemRequestDto(int productId) {
        return new OrderItemRequestDto(0, productId, DEFAULT_QUANTITY);
    }

    public static OrderItemRequestDto[] createValidOrderItems(int count) {
        OrderItemRequestDto[] items = new OrderItemRequestDto[count];
        for (int i = 0; i < count; i++) {
            // ✅ productId 를 순차적으로 증가 (중복 ID 방지)
            items[i] = createValidOrderItemRequestDto(i + 1, i + 1);
        }
        return items;
    }

    // ==================== Builder 패턴 (개선) ====================

    public static OrderCreateRequestDtoBuilder builder() {
        return new OrderCreateRequestDtoBuilder();
    }

    public static class OrderCreateRequestDtoBuilder {
        private String email = VALID_EMAIL;
        private String address = VALID_ADDRESS;
        private String zipCode = VALID_ZIP_CODE;
        private final List<OrderItemRequestDto> items = new ArrayList<>();

        // ✅ 기본 아이템 1 개 자동 추가
        {
            items.add(createValidOrderItemRequestDto());
        }

        public OrderCreateRequestDtoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public OrderCreateRequestDtoBuilder address(String address) {
            this.address = address;
            return this;
        }

        public OrderCreateRequestDtoBuilder zipCode(String zipCode) {
            this.zipCode = zipCode;
            return this;
        }

        // ✅ 아이템 추가 (누적)
        public OrderCreateRequestDtoBuilder addItem(OrderItemRequestDto item) {
            this.items.add(item);
            return this;
        }

        public OrderCreateRequestDtoBuilder addDefaultItem(int productId) {
            this.items.add(createValidOrderItemRequestDto(productId));
            return this;
        }

        // ✅ 아이템 교체 (기존 삭제 후 새로운 항목으로)
        public OrderCreateRequestDtoBuilder items(OrderItemRequestDto... items) {
            this.items.clear();
            this.items.addAll(List.of(items));
            return this;
        }

        // ✅ 아이템 초기화 (빈 배열)
        public OrderCreateRequestDtoBuilder clearItems() {
            this.items.clear();
            return this;
        }

        public OrderCreateRequestDto build() {
            return new OrderCreateRequestDto(
                    email,
                    new OrderStatementRequestDto(0, address, zipCode,
                            items.toArray(OrderItemRequestDto[]::new))
            );
        }
    }

    // ==================== 검증 실패용 데이터 ====================

    // --- Email 검증 실패 ---
    public static OrderCreateRequestDto createRequestDtoWithEmailNull() {
        return new OrderCreateRequestDto(null, createValidOrderStatementRequestDto());
    }

    public static OrderCreateRequestDto createRequestDtoWithEmailBlank() {
        return new OrderCreateRequestDto(" ", createValidOrderStatementRequestDto());
    }

    public static OrderCreateRequestDto createRequestDtoWithEmailInvalidFormat() {
        return new OrderCreateRequestDto("invalid-email", createValidOrderStatementRequestDto());
    }

    // --- OrderStatement 검증 실패 ---
    public static OrderCreateRequestDto createRequestDtoWithOrderStatementsNull() {
        return new OrderCreateRequestDto(VALID_EMAIL, null);
    }

    // --- Address 검증 실패 ---
    public static OrderCreateRequestDto createRequestDtoWithAddressNull() {
        return new OrderCreateRequestDto(
                VALID_EMAIL,
                new OrderStatementRequestDto(0, null, VALID_ZIP_CODE,
                        new OrderItemRequestDto[]{createValidOrderItemRequestDto()})
        );
    }

    public static OrderCreateRequestDto createRequestDtoWithAddressBlank() {
        return new OrderCreateRequestDto(
                VALID_EMAIL,
                new OrderStatementRequestDto(0, " ", VALID_ZIP_CODE,
                        new OrderItemRequestDto[]{createValidOrderItemRequestDto()})
        );
    }

    // --- ZipCode 검증 실패 ---
    public static OrderCreateRequestDto createRequestDtoWithZipCodeNull() {
        return new OrderCreateRequestDto(
                VALID_EMAIL,
                new OrderStatementRequestDto(0, VALID_ADDRESS, null,
                        new OrderItemRequestDto[]{createValidOrderItemRequestDto()})
        );
    }

    public static OrderCreateRequestDto createRequestDtoWithZipCodeInvalidLength() {
        return createRequestDtoWithMultipleErrors(VALID_EMAIL, VALID_ADDRESS, ZIP_CODE_TOO_SHORT);
    }

    public static OrderCreateRequestDto createRequestDtoWithZipCodeTooLong() {
        return createRequestDtoWithMultipleErrors(VALID_EMAIL, VALID_ADDRESS, ZIP_CODE_TOO_LONG);
    }

    // --- OrderItems 검증 실패 ---
    public static OrderCreateRequestDto createRequestDtoWithOrderItemsNull() {
        return new OrderCreateRequestDto(
                VALID_EMAIL,
                new OrderStatementRequestDto(0, VALID_ADDRESS, VALID_ZIP_CODE, null)
        );
    }

    public static OrderCreateRequestDto createRequestDtoWithOrderItemsEmpty() {
        return new OrderCreateRequestDto(
                VALID_EMAIL,
                new OrderStatementRequestDto(0, VALID_ADDRESS, VALID_ZIP_CODE,
                        new OrderItemRequestDto[]{})
        );
    }

    // --- ProductId 검증 실패 ---
    public static OrderCreateRequestDto createRequestDtoWithProductIdInvalid() {
        return new OrderCreateRequestDto(
                VALID_EMAIL,
                new OrderStatementRequestDto(0, VALID_ADDRESS, VALID_ZIP_CODE,
                        new OrderItemRequestDto[]{new OrderItemRequestDto(0, 0, 2)})
        );
    }

    public static OrderCreateRequestDto createRequestDtoWithProductIdNegative() {
        return new OrderCreateRequestDto(
                VALID_EMAIL,
                new OrderStatementRequestDto(0, VALID_ADDRESS, VALID_ZIP_CODE,
                        new OrderItemRequestDto[]{new OrderItemRequestDto(0, -1, 2)})
        );
    }

    // --- Quantity 검증 실패 ---
    public static OrderCreateRequestDto createRequestDtoWithQuantityZero() {
        return new OrderCreateRequestDto(
                VALID_EMAIL,
                new OrderStatementRequestDto(0, VALID_ADDRESS, VALID_ZIP_CODE,
                        new OrderItemRequestDto[]{new OrderItemRequestDto(0, 1, 0)})
        );
    }

    public static OrderCreateRequestDto createRequestDtoWithQuantityNegative() {
        return new OrderCreateRequestDto(
                VALID_EMAIL,
                new OrderStatementRequestDto(0, VALID_ADDRESS, VALID_ZIP_CODE,
                        new OrderItemRequestDto[]{new OrderItemRequestDto(0, 1, -1)})
        );
    }

    // --- 복합 오류 ---
    public static OrderCreateRequestDto createRequestDtoWithMultipleErrors(
            String email, String address, String zipCode) {
        return new OrderCreateRequestDto(
                email,
                new OrderStatementRequestDto(0, address, zipCode,
                        new OrderItemRequestDto[]{createValidOrderItemRequestDto()})
        );
    }

    // ==================== Response DTO 팩토리 (신규) ====================

    public static OrderCreateResponseDto createValidCreateResponseDto(int orderId, String email) {
        return new OrderCreateResponseDto(orderId, email, LocalDateTime.now());
    }

    public static OrderCreateResponseDto createValidCreateResponseDto(int orderId) {
        return createValidCreateResponseDto(orderId, VALID_EMAIL);
    }

    public static OrderQueryResponseDto createValidQueryResponseDto(int orderId, String email) {
        return new OrderQueryResponseDto(
                orderId,
                email,
                new OrderStatementResponseDto[]{createValidStatementResponseDto()}
        );
    }

    public static OrderStatementResponseDto createValidStatementResponseDto() {
        return new OrderStatementResponseDto(
                1,
                VALID_ADDRESS,
                VALID_ZIP_CODE,
                List.of(createValidItemResponseDto())
        );
    }

    public static OrderItemResponseDto createValidItemResponseDto() {
        return new OrderItemResponseDto(
                1,
                null, // ProductItemResponseDto 는 테스트 상황에 따라 생성
                DEFAULT_QUANTITY,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}