package za.co.espaza.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AdjustmentRequest {
    @NotBlank(message = "Product id is required")
    private String productId;
    @NotNull(message = "Quantity change is required")
    private Integer quantityChange;
    @NotBlank(message = "Adjustment notes are required")
    @Size(max = 1000, message = "Adjustment notes must be 1000 characters or fewer")
    private String notes;

    public AdjustmentRequest(){

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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
