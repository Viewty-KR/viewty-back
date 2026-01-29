package com.viewty.viewtyback.service;

import com.viewty.viewtyback.entity.Product;
import com.viewty.viewtyback.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public List<Product> findAll(String name){
        if (name == null || name.trim().isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.findByNameContaining(name);
    }
}
