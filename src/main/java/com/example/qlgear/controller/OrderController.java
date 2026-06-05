package com.example.qlgear.controller;

import com.example.qlgear.entity.User;
import com.example.qlgear.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

        User user =
                (User)
                        session.getAttribute("user");

        model.addAttribute(

                "orders",

                service.getOrder_user(user)

        );
        return "my-orders";
    }
}
