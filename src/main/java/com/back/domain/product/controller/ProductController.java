package com.back.domain.product.controller;

import com.back.domain.product.dto.ProductItemResponseDto;
import com.back.domain.product.dto.query.ProductQueryResponseDto;
import com.back.domain.product.entity.Product;
import com.back.domain.product.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Product", description = "제품 처리 API")
@RequestMapping("/api/v1/product")
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ProductQueryResponseDto findAll() {
        List<Product> products = this.productService.findAll();

        List<ProductItemResponseDto> itemDtos = products.stream()
                .map(ProductItemResponseDto::from)
                .toList();

        return new ProductQueryResponseDto(itemDtos);
    }
}
