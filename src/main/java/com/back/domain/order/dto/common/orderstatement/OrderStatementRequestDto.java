package com.back.domain.order.dto.common.orderstatement;

import com.back.domain.order.dto.common.orderitem.OrderItemRequestDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderStatementRequestDto(
        int id,

        @NotBlank(message = "주소는 필수입니다")
        String address,

        @NotNull(message = "우편번호는 필수입니다")
        @Size(min = 5, max = 5, message = "우편번호는 5자리여야 합니다")
        String zipCode,

        @NotNull(message = "주문 항목은 필수입니다")
        OrderItemRequestDto[] orderItems
) {

}
