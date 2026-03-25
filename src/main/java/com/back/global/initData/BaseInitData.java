package com.back.global.initData;

import com.back.domain.product.repository.ProductRepository;
import com.back.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Profile({"local", "dev"})
public class BaseInitData {
    private final ProductService productService;
    private final ProductRepository productRepository;

    @Bean
    public ApplicationRunner initData() {
        return this::init;
    }

    private void init(ApplicationArguments args) {
        if (productService.count() == 4) {
            return;
        }
        productRepository.deleteAll();
        productService.addProduct("커피콩 1", 5000, 1);
        productService.addProduct("커피콩 2", 6000, 2);
        productService.addProduct("커피콩 3", 7000, 3);
        productService.addProduct("커피콩 4", 8000, 4);
    }
}
