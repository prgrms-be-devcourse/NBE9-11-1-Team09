package com.back.domain.order.repository;

import com.back.domain.order.entity.CoffeeOrder;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CoffeeOrder, Integer> {
    boolean existsByEmail(String email);
    Optional<CoffeeOrder> findByEmail(String email);
    Optional<CoffeeOrder> findByEmailAndCreateDateBetween(String email, LocalDateTime start, LocalDateTime end);
}