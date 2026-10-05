package za.co.espaza.backend.dto.response;

import za.co.espaza.backend.entity.SaleItem;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleItemResponse(
        UUID saleItemId,
        String productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {

    public static SaleItemResponse from(SaleItem item) {
        return from(item, null);
    }

    public static SaleItemResponse from(SaleItem item, String productName) {
        return new SaleItemResponse(
                item.getSaleItemId(),
                item.getProductId(),
                productName,
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
