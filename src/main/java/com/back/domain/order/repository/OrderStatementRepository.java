package com.back.domain.order.repository;

import com.back.domain.order.entity.OrderStatement;
import com.back.domain.order.entity.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatementRepository extends JpaRepository<OrderStatement, Integer> {
    List<OrderStatement> findByStatusAndCreateDateBefore(OrderStatus status, LocalDateTime createDate);
}