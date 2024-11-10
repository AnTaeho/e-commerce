package com.example.e_commerce.product.controller;

import com.example.e_commerce.product.controller.dto.BuyRequest;
import com.example.e_commerce.product.controller.dto.ProductRegisterRequest;
import com.example.e_commerce.product.controller.dto.ProductResponse;
import com.example.e_commerce.product.service.ProductService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> registerProduct(@RequestBody ProductRegisterRequest productRegisterRequest) {
        return ResponseEntity.ok(productService.registerProduct(productRegisterRequest));
    }

    @PostMapping("/sell")
    public ResponseEntity<Void> sellProduct(@RequestBody BuyRequest buyRequest) {
        String email = UUID.randomUUID().toString();
        productService.sell(buyRequest, email);
        return ResponseEntity.ok().body(null);
    }

}
