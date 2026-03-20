package com.back.domain.order.dto.common.orderitem;

import com.back.domain.product.dto.ProductItemResponseDto;

import java.time.LocalDateTime;

public record OrderItemResponseDto (
        int id,
        ProductItemResponseDto productItem,
        int quantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){

}
