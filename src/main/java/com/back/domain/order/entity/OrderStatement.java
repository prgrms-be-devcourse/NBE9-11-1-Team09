package com.back.domain.order.entity;

import com.back.domain.product.entity.Product;
import com.back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OrderStatement extends BaseEntity {
    private String address;
    private String zipCode;

    // OrderStatement N : 1 Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @ToString.Exclude
    private CoffeeOrder order;

    // OrderStatement 1 : N OrderItem
    // OrderItem 쪽에 'orderStatement' 필드가 매핑됨
    @OneToMany(mappedBy = "orderStatement", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // toString 무한 루프 방지
    private List<OrderItem> orderItems = new ArrayList<>();

    public OrderStatement(String address, String zipCode, CoffeeOrder order) {
        this.address = address;
        this.zipCode = zipCode;
        this.order = order;
    }

    public OrderItem addOrderItem(int quantity, Product product) {
        OrderItem orderItem = new OrderItem(this, product, quantity);
        orderItems.add(orderItem);
        return orderItem;
    }

    // 배송지별 주문서
    public int getTotalAmount() {
        // 해당 주문서에 담긴 모든 아이템 가격의 합 반환
        return orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }
}