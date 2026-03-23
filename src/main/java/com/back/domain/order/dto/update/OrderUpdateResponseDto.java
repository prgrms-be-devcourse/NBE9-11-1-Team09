package com.back.domain.order.dto.update;

import com.back.domain.order.entity.OrderStatement;

import java.time.LocalDateTime;

public record OrderUpdateResponseDto(
        Integer orderStatementId,
        String email,
        LocalDateTime createdAt
) {
    public static OrderUpdateResponseDto from(OrderStatement statement, String email) {
        return new OrderUpdateResponseDto(
                statement.getId(),
                email,
                statement.getCreateDate()
        );
    }
}