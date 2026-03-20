package com.back.domain.order.dto.update;

import com.back.domain.order.entity.CoffeeOrder;

import java.time.LocalDateTime;

public record OrderUpdateResponseDto (
        int orderId,
        String email,
        LocalDateTime createdAt
) {
    public static OrderUpdateResponseDto from(CoffeeOrder order) {
        return new OrderUpdateResponseDto(
                order.getId(),
                order.getEmail(),
                order.getCreateDate()
        );
    }
}
