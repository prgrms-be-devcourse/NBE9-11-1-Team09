package com.back.domain.order.dto.common.orderitem;

import com.back.domain.order.entity.OrderItem;
import com.back.domain.product.dto.ProductItemResponseDto;
import java.time.LocalDateTime;

public record OrderItemResponseDto(
        int id,
        ProductItemResponseDto productItem,
        int quantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OrderItemResponseDto from(OrderItem orderItem) {
        return new OrderItemResponseDto(
                orderItem.getId(),
                ProductItemResponseDto.from(orderItem.getProduct()),
                orderItem.getQuantity(),
                orderItem.getCreateDate(),
                orderItem.getModifyDate()
        );
    }
}