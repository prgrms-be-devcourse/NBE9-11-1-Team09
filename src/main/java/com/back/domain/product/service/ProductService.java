package com.back.domain.product.service;

import com.back.domain.product.entity.Product;
import com.back.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;

    public Product addProduct(String name, Integer price, Integer imageSequence) {
        Product product = new Product(name, price, imageSequence);
        return productRepository.save(product);
    }

    public long count() {
        return productRepository.count();
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product productExists(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(
                        "상품을 찾을 수 없습니다: " + productId
                ));
    }
    // findAll 로 했을때 => 순서 보장이 안될 수 있다하여 일단 추가했습니다.
    public Product findById(int id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품 없음"));
    }


}
