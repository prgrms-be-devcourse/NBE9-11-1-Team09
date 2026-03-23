package com.back.domain.order.dto.update;

import com.back.domain.order.dto.common.orderstatement.OrderStatementRequestDto;

public record OrderUpdateRequestDto(
        String email,
        OrderStatementRequestDto orderStatements
){
}
        OrderStatementRequestDto orderStatement
) {

}