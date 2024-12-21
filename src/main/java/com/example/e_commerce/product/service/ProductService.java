package com.example.e_commerce.product.service;

import com.example.e_commerce.aop.lock.DistributedLock;
import com.example.e_commerce.history.domain.History;
import com.example.e_commerce.history.repository.HistoryRepository;
import com.example.e_commerce.product.controller.dto.BuyRequest;
import com.example.e_commerce.product.controller.dto.ProductRegisterRequest;
import com.example.e_commerce.product.controller.dto.ProductResponse;
import com.example.e_commerce.product.domain.Product;
import com.example.e_commerce.product.repository.ProductRepository;
import com.example.e_commerce.user.domain.User;
import com.example.e_commerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;

    @Transactional
    public ProductResponse registerProduct(ProductRegisterRequest request) {
        Product product = new Product(request.name(), request.price(), request.amount());
        Product savedProduct = productRepository.save(product);
        return new ProductResponse(savedProduct.getId());
    }

    @Transactional
    @DistributedLock(key = "PRODUCT_LOCK")
    public void sell(BuyRequest buyRequest, String email) {
        User user = getUser(email);
        Product product = getProduct(buyRequest.productId());
        product.sell(buyRequest.amount());
        historyRepository.save(new History(user.getId(), product.getId(), buyRequest.amount()));
    }

    private Product getProduct(Long productId) {
        return productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new IllegalArgumentException("제품을 찾을 수 없습니다."));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
    }

}
