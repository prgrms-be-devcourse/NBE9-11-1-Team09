package com.back.domain.order.dto.common.orderstatement;

import com.back.domain.order.dto.common.orderitem.OrderItemRequestDto;

public record OrderStatementRequestDto(
        int id,
        String address,
        String zipCode,
        OrderItemRequestDto[] orderItems
) {

}
