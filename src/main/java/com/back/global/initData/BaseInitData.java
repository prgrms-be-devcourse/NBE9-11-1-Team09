package com.back.global.initData;

import com.back.domain.product.entity.Product;
import com.back.domain.product.service.ProductService;
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

}
