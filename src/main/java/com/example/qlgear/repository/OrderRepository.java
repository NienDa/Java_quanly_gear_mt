package com.example.qlgear.repository;

import com.example.qlgear.entity.OrderEntity;
import com.example.qlgear.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity,Long> {
    List<OrderEntity> findByUser(User user);
}
