package com.back.domain.order.dto.create;

import com.back.domain.order.dto.common.orderstatement.OrderStatementRequestDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderCreateRequestDto (
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        String email,

        @NotNull(message = "주문 명세는 필수입니다")
        OrderStatementRequestDto orderStatements
) {

}

