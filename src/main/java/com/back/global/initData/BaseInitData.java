package com.back.global.initData;

import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.service.ProductService;
import com.back.domain.order.entity.OrderStatement;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    @Autowired
    @Lazy
    private BaseInitData self;
    private final ProductService productService;

    @Bean
    public ApplicationRunner initData() {
        return args -> {
            self.work1();
            self.work2();
        };
    }

    @Transactional
    public void work1() {
            if(productService.count() > 0) {
                return;
            }

        Product product1 = productService.addProduct("커피콩1", 5000,1);
        Product product2 = productService.addProduct("커피콩2", 6000,2);
        Product product3 = productService.addProduct("커피콩3", 7000,3);
        Product product4 = productService.addProduct("커피콩4", 8000,4);
    }

    //테스트를 수행하기 위해 임의의 데이터 추가 -강지혜
    private final OrderRepository orderRepository;

    @Transactional
    public void work2() {
        if (orderRepository.count() > 0) {
            return;
        }

        Product product1 = productService.findById(1);
        Product product2 = productService.findById(2);
        Product product3 = productService.findById(3);
        Product product4 = productService.findById(4);

        CoffeeOrder order1 = new CoffeeOrder("test@test.com");
        OrderStatement os1 = order1.addOrderStatement("서울", "12345");
        os1.addOrderItem(2, product1);
        os1.addOrderItem(1, product2);

        CoffeeOrder order2 = new CoffeeOrder("user@test.com");
        order2.addOrderStatement("부산", "54321");

        orderRepository.save(order1);
        orderRepository.save(order2);
    }

}
