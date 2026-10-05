package za.co.espaza.backend.entity;

import jakarta.persistence.*;
import za.co.espaza.backend.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @Column(name = "productId", length = 36, nullable = false, updatable = false)
    private String productId;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(length = 100, unique = true)
    private String barcode;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;
    @Column(precision = 10, scale = 2)
    private BigDecimal costPrice;
    @Column(nullable = false)
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryId")
    private Category category;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false)
    private Boolean isActive;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        if (this.productId == null) {
            this.productId = UUID.randomUUID().toString();
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.stockQuantity == null) {
            this.stockQuantity = 0;
        }
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isLowStock() {
        return lowStockThreshold != null && stockQuantity <= lowStockThreshold;
    }

    public void deductStock(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (qty > stockQuantity) {
            throw new BusinessRuleException(
                    "Insufficient stock for '" + name + "': requested " + qty
                            + ", available " + stockQuantity);
        }
        this.stockQuantity -= qty;
    }

    public void addStock(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.stockQuantity += qty;
    }


    public Product() {

    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold= lowStockThreshold;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
