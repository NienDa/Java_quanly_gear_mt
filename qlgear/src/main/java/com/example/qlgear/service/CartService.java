package com.example.qlgear.service;

import com.example.qlgear.entity.*;
import com.example.qlgear.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService {

    private final CartItemRepository cartItemRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final PaymentRepository paymentRepo;

    public CartService(CartItemRepository cartItemRepo,
                       OrderRepository orderRepo,
                       OrderItemRepository orderItemRepo,
                       PaymentRepository paymentRepo) {
        this.cartItemRepo = cartItemRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.paymentRepo = paymentRepo;
    }

    public List<CartItem> getCartItems(User user) {
        return cartItemRepo.findByUser(user);
    }

    public void addToCart(User user, Product product, int quantity) {
        Optional<CartItem> existingItemOpt = cartItemRepo.findByUserAndProduct(user, product);
        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepo.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cartItemRepo.save(newItem);
        }
    }

    public void updateQuantity(Long cartItemId, int quantity) {
        CartItem item = cartItemRepo.findById(cartItemId).orElse(null);
        if (item != null) {
            if (quantity > 0) {
                item.setQuantity(quantity);
                cartItemRepo.save(item);
            } else {
                cartItemRepo.delete(item);
            }
        }
    }

    public void removeFromCart(Long cartItemId) {
        cartItemRepo.deleteById(cartItemId);
    }

    public OrderEntity checkout(User user, String paymentMethod) {
        List<CartItem> cartItems = cartItemRepo.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng của bạn đang trống!");
        }

        // 1. Tạo đơn hàng mới
        OrderEntity order = new OrderEntity();
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setUser(user);
        order.setTotalPrice(0.0); // Sẽ được tự động cập nhật bởi Trigger DB
        OrderEntity savedOrder = orderRepo.save(order);

        double totalAmount = 0.0;

        // 2. Chuyển chi tiết giỏ hàng thành chi tiết đơn hàng
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getProduct().getPrice());
            orderItem.setSubtotal(cartItem.getQuantity() * cartItem.getProduct().getPrice());
            
            totalAmount += orderItem.getSubtotal();
            
            // Lưu order item (Trigger trg_check_inventory và trg_reduce_inventory sẽ tự động chạy tại đây)
            orderItemRepo.save(orderItem);
        }

        // 3. Tạo thông tin thanh toán
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(totalAmount);
        payment.setStatus("COD".equalsIgnoreCase(paymentMethod) ? "PENDING" : "COMPLETED");
        paymentRepo.save(payment);

        // 4. Xóa sạch giỏ hàng của người dùng
        cartItemRepo.deleteByUser(user);

        return savedOrder;
    }
}
