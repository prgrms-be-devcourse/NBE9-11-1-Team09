package com.back.domain.order.exception;

public class OrderStatementNotFoundException extends NotFoundException {
    public OrderStatementNotFoundException() {
        super("존재하지않는 OrderStatement_Id입니다.");
    }
}
