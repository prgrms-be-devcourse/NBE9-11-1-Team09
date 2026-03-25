package com.back.domain.order.entity;

import com.back.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "coffee_orders")
public class CoffeeOrder extends BaseEntity {
    @Column(nullable = false)
    private String email;

    // 기본 상태: 대기
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

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

    public LocalDate getExpectedShippingDate() {
        // 데이터베이스 저장 전 예외 처리
        if (this.createDate == null) {
            return LocalDate.now();
        }

        if (this.createDate.getHour() < 14) {
            // 오늘
            return this.createDate.toLocalDate();
        } else {
            // 내일
            return this.createDate.toLocalDate().plusDays(1);
        }
    }

    public void markAsShipped() {
        this.status = OrderStatus.SHIPPED;
    }
}
