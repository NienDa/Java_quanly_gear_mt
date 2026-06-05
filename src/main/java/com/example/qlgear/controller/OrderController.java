package com.example.qlgear.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.qlgear.entity.User;
import com.example.qlgear.service.OrderService;

import jakarta.servlet.http.HttpSession;

@Controller
public class OrderController {
    private final OrderService service;

    public OrderController(
            OrderService service
    ) {
        this.service = service;
    }

    @GetMapping("/my-orders")
    public String myOrders(
            HttpSession session,
            Model model
    ){
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute(
                "orders",
                service.getOrder_user(user)
        );
        return "my-orders";
    }

    // admin xem danh sách đơn hàng
    @GetMapping("/admin/orders")
    public String adminOrders(Model model, HttpSession session){
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role) && !"STAFF".equals(role)) return "redirect:/";

        model.addAttribute("orders", service.getAllOrders());
        return "admin-orders";
    }

    //admin cập nhật
    @PostMapping("/admin/orders/update/{id}")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status, HttpSession session){
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role) && !"STAFF".equals(role)) return "redirect:/";

        service.updateOrderStatus(id, status);
        return "redirect:/admin/orders";
    }
}
