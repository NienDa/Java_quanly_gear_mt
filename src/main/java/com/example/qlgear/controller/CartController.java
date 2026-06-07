package com.example.qlgear.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.qlgear.entity.CartItem;
import com.example.qlgear.entity.OrderEntity;
import com.example.qlgear.entity.Payment;
import com.example.qlgear.entity.Product;
import com.example.qlgear.entity.User;
import com.example.qlgear.repository.PaymentRepository;
import com.example.qlgear.service.CartService;
import com.example.qlgear.service.OrderService;
import com.example.qlgear.service.ProductService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

    private final CartService cartService;
    private final ProductService productService;
    private final OrderService orderService;
    private final PaymentRepository paymentRepo;

    public CartController(CartService cartService,
                          ProductService productService,
                          OrderService orderService,
                          PaymentRepository paymentRepo) {
        this.cartService = cartService;
        this.productService = productService;
        this.orderService = orderService;
        this.paymentRepo = paymentRepo;
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<CartItem> cartItems = cartService.getCartItems(user);
        model.addAttribute("cartItems", cartItems);

        double total = cartItems.stream()
                .mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice())
                .sum();
        model.addAttribute("total", total);

        return "cart";
    }

    @PostMapping("/cart/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes,
                            @RequestHeader(value = "referer", required = false) String referer) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        if (quantity <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Thêm vào giỏ hàng thất bại: Số lượng phải lớn hơn 0");
            if (referer != null && !referer.isEmpty()) {
                return "redirect:" + referer;
            }
            return "redirect:/";
        }
        Product product = productService.getProductById(productId);
        try {
            cartService.addToCart(user, product, quantity);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Thêm vào giỏ hàng thất bại: " + e.getMessage());
        }
        
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }

    @PostMapping("/cart/update")
    public String updateQuantity(@RequestParam Long cartItemId,
                                 @RequestParam int quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        if (quantity <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật thất bại: Số lượng phải lớn hơn 0");
            return "redirect:/cart";
        }
        try {
            cartService.updateQuantity(cartItemId, quantity);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cập nhật số lượng thất bại: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    @GetMapping("/cart/delete/{id}")
    public String deleteItem(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        cartService.removeFromCart(id);
        return "redirect:/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(@RequestParam String paymentMethod,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            OrderEntity order = cartService.checkout(user, paymentMethod);
            redirectAttributes.addAttribute("orderId", order.getId());
            return "redirect:/cart/success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Thanh toán thất bại: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/cart/success")
    public String checkoutSuccess(@RequestParam Long orderId,
                                  HttpSession session,
                                  Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        OrderEntity order = orderService.getOrder_user(user).stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElse(null);
        if (order == null) {
            return "redirect:/";
        }
        model.addAttribute("order", order);

        Payment payment = paymentRepo.findByOrder(order).orElse(null);
        model.addAttribute("payment", payment);

        return "checkout-success";
    }
}
