package com.back.domain.order.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.entity.OrderStatement;
import com.back.domain.order.entity.OrderStatus;
import com.back.domain.order.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderShippingSchedulerTest {

    @Autowired
    private OrderShippingScheduler orderShippingScheduler;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setUp() {
        // 기존 데이터가 테스트에 영향을 주지 않도록 초기화
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("성공: 14시 이전 PENDING 주문만 SHIPPED로 변경")
    void processShippingBefore2PM_Success() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime before2PM = today.withHour(13).withMinute(0); // 13시
        LocalDateTime after2PM = today.withHour(15).withMinute(0);  // 15시

        CoffeeOrder order1 = new CoffeeOrder("dnclsehd122@gmail.com");
        OrderStatement statement1 = order1.addOrderStatement("창원시 성산구", "51427");

        CoffeeOrder order2 = new CoffeeOrder("dnclsehd123@naver.com");
        OrderStatement statement2 = order2.addOrderStatement("세종특별자치시 조치원읍", "51427");

        orderRepository.save(order1);
        orderRepository.save(order2);

        // 자동으로 생성된 시간을 2시 이전과 이후로 덮어쓰기
        ReflectionTestUtils.setField(statement1, "createDate", before2PM);
        ReflectionTestUtils.setField(statement2, "createDate", after2PM);

        // 변경된 시간을 강제 업데이트
        orderRepository.saveAndFlush(order1);
        orderRepository.saveAndFlush(order2);
        em.clear();

        // 오후 2시에 스케줄러가 호출되었다고 가정, 수동으로 메서드 호출
        orderShippingScheduler.processShippingBefore2PM();

        CoffeeOrder savedOrder1 = orderRepository.findById(order1.getId()).orElseThrow();
        CoffeeOrder savedOrder2 = orderRepository.findById(order2.getId()).orElseThrow();

        // 14시 이전 주문은 배송 중(SHIPPED)으로 변경되어야 함
        assertThat(savedOrder1.getStatements().get(0).getStatus()).isEqualTo(OrderStatus.SHIPPED);

        // 14시 이후 주문은 여전히 대기 중(PENDING)이어야 함
        assertThat(savedOrder2.getStatements().get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("성공: 이미 SHIPPED 상태인 주문은 스케줄러가 다시 변경하지 않음")
    void processShipping_IgnoreAlreadyShipped() {
        CoffeeOrder order = new CoffeeOrder("dnclsehd122@gmail.com");
        OrderStatement statement = order.addOrderStatement("창원시 성산구", "51427");

        // 이미 배송중 처리
        statement.markAsShipped();
        orderRepository.save(order);

        // 10시에 주문했지만 이미 배송중인 상태
        ReflectionTestUtils.setField(statement, "createDate", LocalDateTime.now().withHour(10));
        orderRepository.saveAndFlush(order);
        em.clear();

        // 스케줄러 실행
        orderShippingScheduler.processShippingBefore2PM();

        CoffeeOrder savedOrder = orderRepository.findById(order.getId()).orElseThrow();

        // SHIPPED 상태가 그대로 유지되어야 함
        assertThat(savedOrder.getStatements().get(0).getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("14시 이전 주문의 배송 예정일은 당일")
    void expectedShippingDate_Before2PM() {
        CoffeeOrder order = new CoffeeOrder("dnclsehd122@gmail.com");
        OrderStatement statement = order.addOrderStatement("창원시 성산구", "51427");
        LocalDateTime before2PM = LocalDateTime.now().withHour(13).withMinute(0);

        // 필드 강제 주입
        ReflectionTestUtils.setField(statement, "createDate", before2PM);

        LocalDate expectedDate = statement.getExpectedShippingDate();

        assertThat(expectedDate).isEqualTo(before2PM.toLocalDate());
    }

    @Test
    @DisplayName("14시 이후 주문의 배송 예정일은 다음날")
    void expectedShippingDate_After2PM() {
        CoffeeOrder order = new CoffeeOrder("dnclsehd122@gmail.com");
        OrderStatement statement = order.addOrderStatement("창원시 성산구", "51427");
        LocalDateTime after2PM = LocalDateTime.now().withHour(15).withMinute(0);

        // 필드 강제 주입
        ReflectionTestUtils.setField(statement, "createDate", after2PM);

        LocalDate expectedDate = statement.getExpectedShippingDate();

        assertThat(expectedDate).isEqualTo(after2PM.toLocalDate().plusDays(1));
    }

    @Test
    @DisplayName("예외 처리: createDate가 null일 경우 오늘 날짜를 반환하여 NPE를 방지")
    void expectedShippingDate_NullCreateDate() {
        CoffeeOrder order = new CoffeeOrder("dnclsehd122@gmail.com");
        OrderStatement statement = order.addOrderStatement("창원시 성산구", "51427");
        // createDate 세팅 X (null 상태)

        LocalDate expectedDate = statement.getExpectedShippingDate();

        assertThat(expectedDate).isEqualTo(LocalDate.now());
    }
}