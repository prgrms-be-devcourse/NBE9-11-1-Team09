package com.back.domain.order.dto.update;

import com.back.domain.order.dto.common.orderstatement.OrderStatementRequestDto;

public record OrderUpdateRequestDto  (
        int id,
        String email,
        OrderStatementRequestDto[] orderStatements
) {

}