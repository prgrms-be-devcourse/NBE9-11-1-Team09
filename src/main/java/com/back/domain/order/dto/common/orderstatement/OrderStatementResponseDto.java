package com.back.domain.order.dto.common.orderstatement;

import com.back.domain.order.dto.common.orderitem.OrderItemResponseDto;
import com.back.domain.order.entity.OrderStatement;
import java.util.List;

public record OrderStatementResponseDto(
        int id,
        String address,
        String zipCode,
        List<OrderItemResponseDto> orderItems
) {
    public static OrderStatementResponseDto from(OrderStatement orderStatement) {
        return new OrderStatementResponseDto(
                orderStatement.getId(),
                orderStatement.getAddress(),
                orderStatement.getZipCode(),
                orderStatement.getOrderItems().stream()
                        .map(OrderItemResponseDto::from)
                        .toList()
        );
    }
}
