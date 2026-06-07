package com.example.qlgear.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.qlgear.entity.Inventory;
import com.example.qlgear.entity.Product;
import com.example.qlgear.repository.InventoryRepository;
import com.example.qlgear.repository.ProductRepository;

@Service
@Transactional
public class InventoryService {
    private final InventoryRepository repo;
    private final ProductRepository productRepo;

    public InventoryService(
            InventoryRepository repo,
            ProductRepository productRepo
    ) {
        this.repo = repo;
        this.productRepo = productRepo;
    }
    ///list
    public List<Inventory> getAllInven(){
        return repo.findAll();
    }

    public Inventory
    getInventoryById(Long id){
        return repo.findById(id).orElse(null);
    }
    //edit
    public void edit_Inven(Inventory inventory){
        if (inventory.getQuantity() != null && inventory.getQuantity() < 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0!");
        }
        Inventory i=repo.findById(inventory.getId()).orElse(null);
        if (i != null) {
            i.setQuantity(inventory.getQuantity());
            i.setImportDate(LocalDate.now());
            
            Product product = i.getProduct();
            if (product != null) {
                if (inventory.getQuantity() != null && inventory.getQuantity() <= 0) {
                    product.setStatus("OUT_OF_STOCK");
                } else {
                    product.setStatus("AVAILABLE");
                }
                productRepo.save(product);
            }
            repo.save(i);
        }
    }
}
