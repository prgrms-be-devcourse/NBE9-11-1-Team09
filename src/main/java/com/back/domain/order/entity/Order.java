package com.back.domain.order.entity;

import com.back.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Order extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String email;

    // Order 1 : N OrderStatement
    // OrderStatement 쪽에 'order' 필드가 매핑됨
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // toString 무한 루프 방지
    private List<OrderStatement> statements = new ArrayList<>();

    public Order(String email) {
        this.email = email;
    }

    public OrderStatement addOrderStatement(String address, String zipCode) {
        OrderStatement orderStatement = new OrderStatement(address, zipCode, this);
        statements.add(orderStatement);
        return orderStatement;
    }
}
