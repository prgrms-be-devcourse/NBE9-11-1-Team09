package com.back.domain.order.exception;

public class OrderNotFoundException extends NotFoundException {
    public OrderNotFoundException() {
        super("존재하지않는 Order_Id입니다.");
    }
}
