package com.back.domain.order.entity;

import com.back.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    private Order order;

    // OrderStatement 1 : N OrderItem
    // OrderItem 쪽에 'orderStatement' 필드가 매핑됨
    @OneToMany(mappedBy = "orderStatement", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // toString 무한 루프 방지
    private List<OrderItem> orderItems = new ArrayList<>();

    public OrderStatement(String address, String zipCode, Order order) {
        this.address = address;
        this.zipCode = zipCode;
        this.order = order;
    }
}
