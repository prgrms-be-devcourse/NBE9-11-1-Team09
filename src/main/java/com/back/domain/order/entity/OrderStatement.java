package com.back.domain.order.entity;

import com.back.domain.product.entity.Product;
import com.back.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
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

    // 배송 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

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

    // 총 금액 계산
    public int getTotalAmount() {
        return orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }

    // 배송 예정일 계산
    public LocalDate getExpectedShippingDate() {
        if (this.getCreateDate() == null) {
            return LocalDate.now();
        }

        if (this.getCreateDate().getHour() < 14) {
            // 14시 이전: 오늘
            return this.getCreateDate().toLocalDate();
        } else {
            // 14시 이후: 내일
            return this.getCreateDate().toLocalDate().plusDays(1);
        }
    }

    // 상태 변경
    public void markAsShipped() {
        this.status = OrderStatus.SHIPPED;
    }
}