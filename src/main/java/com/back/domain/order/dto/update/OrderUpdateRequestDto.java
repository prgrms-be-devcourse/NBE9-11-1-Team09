package com.back.domain.order.dto.update;

import com.back.domain.order.dto.orderstatement.OrderStatementRequestDto;

public record OrderUpdateRequestDto(
        String email,
        OrderStatementRequestDto orderStatement
) {

}