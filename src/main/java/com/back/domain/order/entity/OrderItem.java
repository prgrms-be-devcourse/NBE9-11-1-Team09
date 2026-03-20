package com.back.domain.order.entity;

import com.back.domain.product.entity.Product;
import com.back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends BaseEntity {

    // OrderItem N : 1 OrderStatement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_statement_id")
    @ToString.Exclude
    private OrderStatement orderStatement;

    // OrderItem N : 1 Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    private Product product;

    private int quantity;
}