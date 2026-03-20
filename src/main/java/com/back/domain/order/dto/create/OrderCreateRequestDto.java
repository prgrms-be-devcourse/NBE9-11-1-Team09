package com.back.domain.order.dto.create;

import com.back.domain.order.dto.common.orderstatement.OrderStatementRequestDto;

public record OrderCreateRequestDto (
        int id,
        String email,
        OrderStatementRequestDto[] OrderStatements
) {

}

