package com.back.domain.order.exception;

public class OrderNotFountException extends NotFoundException {
    public OrderNotFountException() {
        super("존재하지않는 Order_Id입니다.");
    }
}
