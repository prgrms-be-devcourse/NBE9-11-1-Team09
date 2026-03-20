package com.back.domain.order.repository;

import com.back.domain.order.entity.CoffeeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CoffeeOrder, Integer> {
}
