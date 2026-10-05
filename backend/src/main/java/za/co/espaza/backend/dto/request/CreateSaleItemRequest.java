package za.co.espaza.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateSaleItemRequest(
        @NotBlank(message = "Product id is required") String productId,
        @Min(value = 1, message = "Quantity must be at least 1") int quantity
) {
}
