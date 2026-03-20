package com.back.domain.order.dto.common.orderitem;

public record OrderItemRequestDto (
        int id,
        int productId,
        int quantity
) {

}
