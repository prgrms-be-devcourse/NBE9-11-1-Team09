package com.back.domain.order.factory;


import com.back.domain.order.dto.common.orderitem.OrderItemRequestDto;
import com.back.domain.order.dto.common.orderstatement.OrderStatementRequestDto;
import com.back.domain.order.dto.create.OrderCreateRequestDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderTestDataFactory {

    // ==================== 📌 테스트 데이터 상수 ====================
    public static final String VALID_EMAIL = "test@email.com";
    public static final String VALID_ADDRESS = "서울시 강남구 테헤란로 123";
    public static final String VALID_ZIP_CODE = "12345";
    public static final int DEFAULT_QUANTITY = 2;

    // 검증 경계값
    public static final int ZERO_PRODUCT_ID = 0;
    public static final int NEGATIVE_PRODUCT_ID = -1;
    public static final int ZERO_QUANTITY = 0;
    public static final int NEGATIVE_QUANTITY = -1;
    public static final String ZIP_CODE_TOO_SHORT = "1234";
    public static final String INVALID_EMAIL_FORMAT = "invalid-email";
    public static final String BLANK_STRING = " ";

    // ==================== ✅ 검증 성공 데이터 (Request) ====================
    public static OrderCreateRequestDto createValidRequestDto(String email, int productId, int quantity) {
        return new OrderCreateRequestDto(
                email,
                new OrderStatementRequestDto(0, VALID_ADDRESS, VALID_ZIP_CODE,
                        new OrderItemRequestDto[]{createValidOrderItemRequestDto(productId, quantity)})
        );
    }
    public static OrderCreateRequestDto createValidRequestDto(String email, int productId) {
        return createValidRequestDto(email, productId, DEFAULT_QUANTITY);
    }

    public static OrderCreateRequestDtoBuilder builder() {
        return new OrderCreateRequestDtoBuilder();
    }

    public static class OrderCreateRequestDtoBuilder {
        private String email = VALID_EMAIL;
        private String address = VALID_ADDRESS;
        private String zipCode = VALID_ZIP_CODE;
        private final List<OrderItemRequestDto> items = new ArrayList<>();

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

        public OrderCreateRequestDtoBuilder addItem(int productId, int quantity) {
            this.items.add(createValidOrderItemRequestDto(productId, quantity));
            return this;
        }

        public OrderCreateRequestDtoBuilder addDefaultItem(int productId) {
            this.items.add(createValidOrderItemRequestDto(productId, DEFAULT_QUANTITY));
            return this;
        }

        public OrderCreateRequestDtoBuilder clearItems() {
            this.items.clear();
            return this;
        }

        public OrderCreateRequestDto build() {
            if (items.isEmpty()) {
                throw new IllegalStateException("최소 1 개 이상의 주문 항목이 필요합니다");
            }
            return new OrderCreateRequestDto(
                    email,
                    new OrderStatementRequestDto(0, address, zipCode,
                            items.toArray(OrderItemRequestDto[]::new))
            );
        }
    }

    // ==================== ❌ 검증 실패용 데이터 (Request) ====================
    // Email 검증 실패
    public static OrderCreateRequestDto createRequestDtoWithEmailInvalidFormat(int productId) {
        return new OrderCreateRequestDto(
                INVALID_EMAIL_FORMAT,
                new OrderStatementRequestDto(0, VALID_ADDRESS, VALID_ZIP_CODE,
                        new OrderItemRequestDto[]{createValidOrderItemRequestDto(productId, DEFAULT_QUANTITY)})
        );
    }

    // ZipCode 검증 실패
    public static OrderCreateRequestDto createRequestDtoWithZipCodeTooShort(int productId) {
        return new OrderCreateRequestDto(
                VALID_EMAIL,
                new OrderStatementRequestDto(0, VALID_ADDRESS, ZIP_CODE_TOO_SHORT,
                        new OrderItemRequestDto[]{createValidOrderItemRequestDto(productId, DEFAULT_QUANTITY)})
        );
    }

    // ProductId 검증 실패
    public static OrderCreateRequestDto createRequestDtoWithProductIdZero() {
        return new OrderCreateRequestDto(
                VALID_EMAIL,
                new OrderStatementRequestDto(0, VALID_ADDRESS, VALID_ZIP_CODE,
                        new OrderItemRequestDto[]{
                                new OrderItemRequestDto(0, ZERO_PRODUCT_ID, DEFAULT_QUANTITY)
                        })
        );
    }

    public static OrderCreateRequestDto createRequestDtoWithProductIdNegative() {
        return new OrderCreateRequestDto(
                VALID_EMAIL,
                new OrderStatementRequestDto(0, VALID_ADDRESS, VALID_ZIP_CODE,
                        new OrderItemRequestDto[]{
                                new OrderItemRequestDto(0, NEGATIVE_PRODUCT_ID, DEFAULT_QUANTITY)
                        })
        );
    }

    // Quantity 검증 실패
    public static OrderCreateRequestDto createRequestDtoWithQuantityZero(int productId) {
        return new OrderCreateRequestDto(
                VALID_EMAIL,
                new OrderStatementRequestDto(0, VALID_ADDRESS, VALID_ZIP_CODE,
                        new OrderItemRequestDto[]{
                                new OrderItemRequestDto(0, productId, ZERO_QUANTITY)
                        })
        );
    }

    public static OrderCreateRequestDto createRequestDtoWithQuantityNegative(int productId) {
        return new OrderCreateRequestDto(
                VALID_EMAIL,
                new OrderStatementRequestDto(0, VALID_ADDRESS, VALID_ZIP_CODE,
                        new OrderItemRequestDto[]{
                                new OrderItemRequestDto(0, productId, NEGATIVE_QUANTITY)
                        })
        );
    }

    // ==================== 🔧 내부 헬퍼 메서드 ====================
    private static OrderItemRequestDto createValidOrderItemRequestDto(int productId, int quantity) {
        return new OrderItemRequestDto(0, productId, quantity);
    }
}