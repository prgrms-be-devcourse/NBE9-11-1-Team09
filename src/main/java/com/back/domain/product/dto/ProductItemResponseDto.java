package com.back.domain.product.dto;

import com.back.domain.product.entity.Product;

public record ProductItemResponseDto(
        int productId,
        String name,
        int price,
        int imageSeq
) {
    public static ProductItemResponseDto from(Product product) {
        return new ProductItemResponseDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageSequence()
        );
    }
}
