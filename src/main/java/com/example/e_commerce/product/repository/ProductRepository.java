package com.example.e_commerce.product.repository;

import com.example.e_commerce.product.domain.Product;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select p from Product p where p.id = :productId")
    Optional<Product> findByIdWithLock(@Param("productId") Long productId);

}
