package com.back.domain.product.controller.docs;

import com.back.domain.product.dto.query.ProductQueryResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Product", description = "제품 관리 API")
@RequestMapping("/api/v1/product")
public interface ProductControllerDocs {

    @Operation(
            summary = "전체 상품 조회",
            description = "등록된 모든 상품의 목록을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProductQueryResponseDto.class)
            )
    )
    @GetMapping
    ProductQueryResponseDto findAll();
}
