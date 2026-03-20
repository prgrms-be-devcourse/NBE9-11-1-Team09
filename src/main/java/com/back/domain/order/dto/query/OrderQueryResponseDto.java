package com.back.domain.order.dto.query;

import com.back.domain.order.dto.common.orderstatement.OrderStatementResponseDto;

public record OrderQueryResponseDto (
        int id,
        String email,
        OrderStatementResponseDto[] OrderStatements
) {

}