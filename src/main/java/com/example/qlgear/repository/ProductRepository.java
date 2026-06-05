package com.example.qlgear.repository;

import com.example.qlgear.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Long> {
    List<Product> findByProductNameContainingIgnoreCase(String name);
    List<Product> findByCategoryId(Long id);
}
