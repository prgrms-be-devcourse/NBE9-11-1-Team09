package com.back.domain.order.scheduler;

import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.entity.OrderStatus;
import com.back.domain.order.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderShippingScheduler {

    private final OrderRepository orderRepository;

    // 매일 14시 00분 00초에 실행 (초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 14 * * *")
    @Transactional
    public void processShippingBefore2PM() {
        LocalDateTime today2PM = LocalDateTime.now().withHour(14).withMinute(0).withSecond(0).withNano(0);

        // 상태가 PENDING이면서 오늘 14시 이전에 들어온 주문들 조회
        List<CoffeeOrder> pendingOrders = orderRepository.findByStatusAndCreateDateBefore(
                OrderStatus.PENDING,
                today2PM
        );

        if (pendingOrders.isEmpty()) {
            log.info("오늘 배송할 주문이 없습니다.");
            return;
        }

        // 일괄 배송 상태로 변경
        for (CoffeeOrder order : pendingOrders) {
            order.markAsShipped();
        }

        log.info("총 {}건의 주문이 당일 배송(SHIPPED) 처리되었습니다.", pendingOrders.size());
    }
}