package com.back.domain.order.entity;

public enum OrderStatus {
    PENDING,    // 결제 완료 및 배송 대기 (기본값)
    SHIPPED,    // 배송 중 (오후 2시 이후 변경)
    DELIVERED    // 배송 완료
}