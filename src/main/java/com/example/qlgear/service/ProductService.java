package com.example.qlgear.service;

import com.example.qlgear.entity.Category;
import com.example.qlgear.entity.Product;
import com.example.qlgear.repository.CategoryRepository;
import com.example.qlgear.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repo;
    private final CategoryRepository repoCate;
    public ProductService(ProductRepository repo,CategoryRepository repoCate){
        this.repo=repo;
        this.repoCate=repoCate;
    }
    //get all
    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    // lấy sản phẩm theo id
    public Product getProductById(Long id) {

        return repo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found"
                        ));
    }

    // thêm sản phẩm
    public void addProduct(Product product) {
        Category cat=repoCate.findById(product.getCategory().getId()).orElse(null);
        product.setCategory(cat);
        repo.save(product);
    }

    // cập nhật sản phẩm
    public void updateProduct(Long id, Product newProduct) {

        Product oldProduct = getProductById(id);

        oldProduct.setProductName(newProduct.getProductName());
        oldProduct.setBrand(newProduct.getBrand());
        oldProduct.setDescription(newProduct.getDescription());
        oldProduct.setPrice(newProduct.getPrice());
        oldProduct.setImageUrl(newProduct.getImageUrl());
        oldProduct.setStatus(newProduct.getStatus());
        Category ca=repoCate.findById(newProduct.getCategory().getId()).orElse(null);
        oldProduct.setCategory(newProduct.getCategory());

        repo.save(oldProduct);
    }

    // xóa sản phẩm
    public void deleteProduct(Long id) {

        Product product = getProductById(id);

        repo.delete(product);
    }
    //tìm kiếm bằng name
    public List<Product> find_NameSP(String name){
        return repo.findByProductNameContainingIgnoreCase(name);
    }
    //lọc theo category
    public List<Product> find_byCate(Long id){
        return repo.findByCategoryId(id);
    }

}
