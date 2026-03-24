package com.back.global.initData;

import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.entity.OrderStatement;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Profile({"default", "local", "dev"})
public class BaseInitData {
    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Bean
    public ApplicationRunner initData() {
        return args -> {
            if (productService.count() > 0) return;
            Product p1 = productService.addProduct("커피콩1", 5000, 1);
            Product p2 = productService.addProduct("커피콩2", 6000, 2);
            Product p3 = productService.addProduct("커피콩3", 7000, 3);
            Product p4 = productService.addProduct("커피콩4", 8000, 4);

            // 2. 테스트용 주문 데이터 생성
            CoffeeOrder order1 = new CoffeeOrder("test@test.com");

            OrderStatement os1 = order1.addOrderStatement("서울", "12345");
            os1.addOrderItem(2, p1);
            os1.addOrderItem(1, p2);

            OrderStatement os2 = order1.addOrderStatement("부산", "11111");
            os2.addOrderItem(2, p1);
            os2.addOrderItem(1, p2);

            CoffeeOrder order2 = new CoffeeOrder("user@test.com");
            order2.addOrderStatement("부산", "54321");

            // 3. 저장 (Rollback 되므로 중복 저장 걱정 없음)
            orderRepository.save(order1);
            orderRepository.save(order2);
        };
    }
}
