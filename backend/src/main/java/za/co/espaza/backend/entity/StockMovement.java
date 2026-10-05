package za.co.espaza.backend.entity;


import jakarta.persistence.*;
import za.co.espaza.backend.Enum.MovementType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_movement")
public class StockMovement {
    @Id
    @Column(name = "movementId", length = 36, nullable = false, updatable = false)
    private String movementId;
    @Column(nullable = false, length = 36)
    private String productId;
    @Column(nullable = false, length = 36)
    private String createdBy;
    @Column(nullable = false)
    private Integer quantityChange;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MovementType movementType;
    @Column(length = 36)
    private String referenceId;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public StockMovement() {

    }

    @PrePersist
    protected void onCreate(){
        if(movementId == null){
            movementId = UUID.randomUUID().toString();
        }
        if(createdAt == null){
            createdAt = LocalDateTime.now();
        }
    }

    public String getMovementId() {
        return movementId;
    }

    public void setMovementId(String movementId) {
        this.movementId = movementId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

}
