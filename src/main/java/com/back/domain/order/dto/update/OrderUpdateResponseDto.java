package com.back.domain.order.dto.update;

import com.back.domain.order.entity.CoffeeOrder;
import java.time.LocalDateTime;

public record OrderUpdateResponseDto(
        Integer orderId,
        String email,
        LocalDateTime createdAt
) {
    public static OrderUpdateResponseDto from(CoffeeOrder coffeeOrder) {
        return new OrderUpdateResponseDto(
                coffeeOrder.getId(),
                coffeeOrder.getEmail(),
                coffeeOrder.getCreateDate()
        );
    }
}