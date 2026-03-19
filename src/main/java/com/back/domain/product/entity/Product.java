package com.back.domain.product.entity;

import com.back.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {
    private String name;        // 상품 이름
    private Integer price;          // 현재 상품 가격
    private Integer imageSequence;

    // 주문 항목과의 관계 (양방향)
//    @OneToMany(mappedBy = "product")
//    private List<OrderItem> orderItems = new ArrayList<>();

    public Product(String name, int price, int imageSeq) {
        this.name = name;
        this.price = price;
        this.imageSequence = imageSeq;
    }
}
