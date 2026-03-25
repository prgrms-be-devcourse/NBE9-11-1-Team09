package com.back.domain.product.controller;

import com.back.domain.product.controller.docs.ProductControllerDocs;
import com.back.domain.product.dto.ProductItemResponseDto;
import com.back.domain.product.dto.query.ProductQueryResponseDto;
import com.back.domain.product.entity.Product;
import com.back.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductControllerDocs {

    private final ProductService productService;

    @Override
    public ProductQueryResponseDto findAll() {
        // Service 에서 반환한 Entity 리스트를 DTO 로 변환
        List<Product> products = productService.findAll();

        List<ProductItemResponseDto> itemDtos = products.stream()
                .map(ProductItemResponseDto::from)
                .toList();

        return new ProductQueryResponseDto(itemDtos);
    }
}
