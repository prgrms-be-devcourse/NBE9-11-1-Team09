package com.back.domain.order.dto.orderstatement;

import com.back.domain.order.dto.orderitem.OrderItemRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema()
public record OrderStatementRequestDto(
        @NotBlank(message = "주소는 필수입니다")
        String address,

        @NotNull(message = "우편번호는 필수입니다")
        @Schema(description = "우편 주소", example = "05005")
        @Size(min = 5, max = 5, message = "우편번호는 5자리여야 합니다")
        String zipCode,

        @NotNull(message = "주문 항목은 필수입니다")
        @Valid
        OrderItemRequestDto[] orderItems
) {

}
