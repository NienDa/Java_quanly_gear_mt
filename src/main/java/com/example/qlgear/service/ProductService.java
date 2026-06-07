package com.example.qlgear.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.qlgear.entity.Category;
import com.example.qlgear.entity.Inventory;
import com.example.qlgear.entity.Product;
import com.example.qlgear.repository.CategoryRepository;
import com.example.qlgear.repository.InventoryRepository;
import com.example.qlgear.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository repo;
    private final CategoryRepository repoCate;
    private final InventoryRepository repoInven;

    public ProductService(ProductRepository repo, CategoryRepository repoCate, InventoryRepository repoInven){
        this.repo=repo;
        this.repoCate=repoCate;
        this.repoInven=repoInven;
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
        // Do số lượng tồn kho ban đầu khi tạo mới luôn bằng 0, nên trạng thái mặc định phải là OUT_OF_STOCK
        product.setStatus("OUT_OF_STOCK");
        Product savedProduct = repo.save(product);

        Inventory inven = new Inventory();
        inven.setProduct(savedProduct);
        inven.setQuantity(0);
        inven.setImportDate(LocalDate.now());
        repoInven.save(inven);
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
        oldProduct.setCategory(ca);

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

    // Lọc theo category và sắp xếp theo giá/ngẫu nhiên
    public List<Product> getProducts(Long categoryId, String sort) {
        List<Product> products;
        if (categoryId == null) {
            products = new java.util.ArrayList<>(repo.findAll());
        } else {
            products = new java.util.ArrayList<>(repo.findByCategoryId(categoryId));
        }

        if ("priceAsc".equals(sort)) {
            products.sort((p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
        } else if ("priceDesc".equals(sort)) {
            products.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
        } else {
            java.util.Collections.shuffle(products);
        }
        return products;
    }

}

