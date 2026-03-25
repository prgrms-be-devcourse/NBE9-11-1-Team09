package com.back.domain.product.dto.query;

import com.back.domain.product.dto.ProductItemResponseDto;

import java.util.List;

public record ProductQueryResponseDto(
        List<ProductItemResponseDto> items
) {
}
