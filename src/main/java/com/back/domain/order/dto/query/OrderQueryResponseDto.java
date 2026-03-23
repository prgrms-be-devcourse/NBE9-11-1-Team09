package com.back.domain.order.dto.query;

import com.back.domain.order.dto.common.orderstatement.OrderStatementResponseDto;
import com.back.domain.order.entity.CoffeeOrder;

public record OrderQueryResponseDto (
        int id,
        String email,
        OrderStatementResponseDto[] orderStatements
)
{
    public static OrderQueryResponseDto from(CoffeeOrder order) {
        return new OrderQueryResponseDto(
                order.getId(),
                order.getEmail(),
                order.getStatements().stream()
                        .map(OrderStatementResponseDto::from)
                        .toArray(OrderStatementResponseDto[]::new)
        );
    }
}
