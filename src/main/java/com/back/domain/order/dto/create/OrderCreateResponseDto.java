package com.back.domain.order.dto.create;

import com.back.domain.order.entity.CoffeeOrder;

import java.time.LocalDateTime;

public record OrderCreateResponseDto(
        int orderId,
        String email,
        LocalDateTime createdAt
) {
    public static OrderCreateResponseDto from(CoffeeOrder order) {
        return new OrderCreateResponseDto(
                order.getId(),
                order.getEmail(),
                order.getCreateDate()
        );
    }
}