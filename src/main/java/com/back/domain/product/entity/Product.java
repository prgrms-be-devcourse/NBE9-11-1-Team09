package com.back.domain.product.entity;

import com.back.domain.order.entity.OrderItem;
import com.back.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {
    private String name;        // 상품 이름
    private Integer price;          // 현재 상품 가격
    private Integer imageSequence;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems = new ArrayList<>();

    public Product(String name, int price, int imageSeq) {
        this.name = name;
        this.price = price;
        this.imageSequence = imageSeq;
    }
}
