package com.example.qlgear.controller;

import com.example.qlgear.entity.Inventory;
import com.example.qlgear.service.InventoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class InventoryController {
    private final InventoryService service;

    public InventoryController(
            InventoryService service
    ) {
        this.service = service;
    }

    @GetMapping("/inventory")
    public String inventoryPage(
            Model model,
            HttpSession session
    ){
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role) && !"STAFF".equals(role)) return "redirect:/";

        model.addAttribute(
                "inventories",
                service.getAllInven()
        );

        return "inventory";
    }

    @GetMapping("/inventory/edit/{id}")
    public String editInventory(
            @PathVariable Long id,
            Model model,
            HttpSession session
    ){
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role) && !"STAFF".equals(role)) return "redirect:/";

        model.addAttribute(
                "inventory",
                service.getInventoryById(id)
        );

        return "edit-inventory";
    }

    @PostMapping("/inventory/update")
    public String updateInventory(
            @ModelAttribute Inventory inventory,
            HttpSession session
    ){
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role) && !"STAFF".equals(role)) return "redirect:/";

        service.edit_Inven(inventory);
        return "redirect:/inventory";
    }
}
