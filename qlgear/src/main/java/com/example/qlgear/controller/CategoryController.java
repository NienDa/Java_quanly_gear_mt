package com.example.qlgear.controller;

import com.example.qlgear.entity.Category;
import com.example.qlgear.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CategoryController {
    private final CategoryService service;
    public CategoryController(CategoryService service){
        this.service=service;
    }
    // list
    @GetMapping("/categories")
    public String list(Model model, HttpSession session){
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role) && !"STAFF".equals(role)) return "redirect:/";

        model.addAttribute(
                "categories",
                service.getAllCategory()
        );

        return "categories";
    }

    // add form
    @GetMapping("/categories/add")
    public String add(Model model, HttpSession session){
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role)) return "redirect:/categories";

        model.addAttribute(
                "category",
                new Category()
        );

        return "add-category";
    }

    // save
    @PostMapping("/categories/save")
    public String save(
            @ModelAttribute Category category,
            HttpSession session
    ){
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role)) return "redirect:/categories";

        service.addCate(category);

        return "redirect:/categories";
    }

    // edit form
    @GetMapping("/categories/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model,
            HttpSession session
    ) {
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role)) return "redirect:/categories";

        model.addAttribute(
                "category",
                service.get_Cate_id(id)
        );

        return "edit-category";
    }

    // update
    @PostMapping("/categories/update/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute Category category,
            HttpSession session
    ){
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role)) return "redirect:/categories";
        service.updateCate(category,id);
        return "redirect:/categories";
    }

    // delete
    @GetMapping("/categories/delete/{id}")
    public String delete(
            @PathVariable Long id,
            HttpSession session
    ){
        String role = (String) session.getAttribute("role");
        if (role == null) return "redirect:/login";
        if (!"ADMIN".equals(role)) return "redirect:/categories";
        service.delCate_id(id);
        return "redirect:/categories";
    }
}

