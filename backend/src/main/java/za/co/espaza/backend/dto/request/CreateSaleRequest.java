package za.co.espaza.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import za.co.espaza.backend.enums.PaymentMethod;

import java.util.List;

public record CreateSaleRequest(
        @NotNull(message = "Payment method is required") PaymentMethod paymentMethod,
        @NotEmpty(message = "A sale must contain at least one item") List<@Valid CreateSaleItemRequest> items,
        String notes
) {
}
