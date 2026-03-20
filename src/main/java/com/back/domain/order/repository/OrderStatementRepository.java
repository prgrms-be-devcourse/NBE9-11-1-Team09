package com.back.domain.order.repository;

import com.back.domain.order.entity.OrderStatement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatementRepository extends JpaRepository<OrderStatement, Integer> {
}
