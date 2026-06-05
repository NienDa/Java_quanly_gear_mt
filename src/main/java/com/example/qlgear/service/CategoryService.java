package com.example.qlgear.service;

import com.example.qlgear.entity.Category;
import com.example.qlgear.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository repo;
    public CategoryService(CategoryRepository repo){
        this.repo=repo;
    }
    //get all
    public List<Category> getAllCategory(){
        return repo.findAll();
    }
    //add
    public void addCate(Category ca){
        repo.save(ca);
    }
    //get id
    public Category get_Cate_id(Long id){
        return repo.findById(id).orElse(null);
    }
    //update
    public void updateCate(Category ca,Long id){
        Category c =repo.findById(id).orElse(null);
        if(c!=null){
            c.setCategoryName(ca.getCategoryName());
            c.setDescription(ca.getDescription());
            repo.save(c);
        }
    }
    //dele
    public void delCate_id(Long id){
        repo.deleteById(id);
    }
}
