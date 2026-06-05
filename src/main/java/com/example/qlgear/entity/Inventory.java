package com.example.qlgear.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "inventories")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    @Column(name = "import_date")
    private LocalDate importDate;

    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public Inventory() {
    }

    public Inventory(Long id, Integer quantity, LocalDate importDate, Product product) {
        this.id = id;
        this.quantity = quantity;
        this.importDate = importDate;
        this.product = product;
    }

    public Long getId() {
        return id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public LocalDate getImportDate() {
        return importDate;
    }

    public Product getProduct() {
        return product;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setImportDate(LocalDate importDate) {
        this.importDate = importDate;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}