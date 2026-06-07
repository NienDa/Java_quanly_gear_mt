package com.example.qlgear.repository;

import com.example.qlgear.entity.Inventory;
import com.example.qlgear.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    Optional<Inventory> findByProduct(Product product);
}
