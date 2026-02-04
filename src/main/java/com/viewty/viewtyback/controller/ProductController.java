package com.viewty.viewtyback.controller;

import com.viewty.viewtyback.entity.Product;
import com.viewty.viewtyback.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<Product> getProducts(
            @RequestParam(required = false) String name
    ) {
        return productService.findAll(name);
    }

}
