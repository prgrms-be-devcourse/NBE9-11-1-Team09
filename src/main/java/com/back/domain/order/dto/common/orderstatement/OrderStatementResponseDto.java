package com.back.domain.order.dto.common.orderstatement;

import com.back.domain.order.dto.common.orderitem.OrderItemResponseDto;

public record OrderStatementResponseDto (
        int id,
        String address,
        String zipCode,
        OrderItemResponseDto[] orderItems
) {
}
