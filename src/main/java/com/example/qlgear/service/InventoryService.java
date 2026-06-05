package com.example.qlgear.service;

import com.example.qlgear.entity.Inventory;
import com.example.qlgear.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class InventoryService {
    private final InventoryRepository repo;

    public InventoryService(
            InventoryRepository repo
    ) {
        this.repo = repo;
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
        Inventory i=repo.findById(inventory.getId()).orElse(null);
        i.setQuantity(inventory.getQuantity());
        i.setImportDate(LocalDate.now());
        repo.save(i);
    }
}
