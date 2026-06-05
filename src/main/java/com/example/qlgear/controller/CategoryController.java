package com.example.qlgear.controller;

import com.example.qlgear.entity.Category;
import com.example.qlgear.service.CategoryService;
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
    public String list(Model model){

        model.addAttribute(
                "categories",
                service.getAllCategory()
        );

        return "categories";
    }

    // add form
    @GetMapping("/categories/add")
    public String add(Model model){

        model.addAttribute(
                "category",
                new Category()
        );

        return "add-category";
    }

    // save
    @PostMapping("/categories/save")
    public String save(
            @ModelAttribute Category category
    ){

        service.addCate(category);

        return "redirect:/categories";
    }

    // edit form
    @GetMapping("/categories/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model
    ) {

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
            @ModelAttribute Category category
    ){
        service.updateCate(category,id);
        return "redirect:/categories";
    }

    // delete
    @GetMapping("/categories/delete/{id}")
    public String delete(
            @PathVariable Long id
    ){
        service.delCate_id(id);
        return "redirect:/categories";
    }
}

