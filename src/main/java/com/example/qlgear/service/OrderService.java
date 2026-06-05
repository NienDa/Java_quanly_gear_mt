package com.example.qlgear.service;

import com.example.qlgear.entity.OrderEntity;
import com.example.qlgear.entity.OrderItem;
import com.example.qlgear.entity.Product;
import com.example.qlgear.entity.User;
import com.example.qlgear.repository.OrderItemRepository;
import com.example.qlgear.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository repoO;
    private final OrderItemRepository repoOI;
    public OrderService(OrderRepository repoO,OrderItemRepository repoOI){
        this.repoO=repoO;
        this.repoOI=repoOI;
    }
    //mua
    public void buy_SP(User user, Product pr){
        OrderEntity order = new OrderEntity();
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("COMPLETED");
        order.setTotalPrice(pr.getPrice());
        order.setUser(user);
        repoO.save(order);

        OrderItem oi=new OrderItem();
        oi.setOrder(order);
        oi.setProduct(pr);
        oi.setQuantity(1);
        oi.setSubtotal(pr.getPrice());
        oi.setUnitPrice(oi.getUnitPrice());
        repoOI.save(oi);
    }
    //ds order
    public List<OrderEntity> getOrder_user(User user){
        return repoO.findByUser(user);
    }
}
