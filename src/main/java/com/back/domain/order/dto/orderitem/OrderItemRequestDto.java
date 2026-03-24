package com.back.domain.order.dto.orderitem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequestDto (
        int id,

        @NotNull(message = "상품은 필수입니다.")
        @Min(value = 1, message = "상품은 1개 이상이어야 합니다")
        int productId,

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
        int quantity
) {

}
