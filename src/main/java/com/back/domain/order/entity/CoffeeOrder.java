package com.back.domain.order.entity;

import com.back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "coffee_orders")
public class CoffeeOrder extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String email;

    // Order 1 : N OrderStatement
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<OrderStatement> statements = new ArrayList<>();

    public CoffeeOrder(String email) {
        this.email = email;
    }

    public OrderStatement addOrderStatement(String address, String zipCode) {
        OrderStatement orderStatement = new OrderStatement(address, zipCode, this);
        this.statements.add(orderStatement);
        return orderStatement;
    }

    public Optional<OrderStatement> removeOrderStatement(int statementId) {
        return statements.stream()
                .filter(s -> s.getId() == statementId)
                .findFirst()
                .map(statement -> {
                    statements.remove(statement);
                    statement.setOrder(null); // 양방향 관계 정리
                    return statement;
                });
    }
}