package com.example.qlgear.controller;

import java.util.List;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.qlgear.entity.CartItem;
import com.example.qlgear.entity.User;
import com.example.qlgear.service.CartService;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CartService cartService;

    public GlobalControllerAdvice(CartService cartService) {
        this.cartService = cartService;
    }

    @ModelAttribute("cartCount")
    public int getCartCount(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return 0;
        }
        List<CartItem> cartItems = cartService.getCartItems(user);
        if (cartItems == null) {
            return 0;
        }
        return cartItems.stream().mapToInt(CartItem::getQuantity).sum();
    }
}
