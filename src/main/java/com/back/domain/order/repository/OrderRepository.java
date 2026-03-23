package com.back.domain.order.repository;

import com.back.domain.order.entity.CoffeeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<CoffeeOrder, Integer> {
    boolean existsByEmail(String email);
    Optional<CoffeeOrder> findByEmail(String email);
}
