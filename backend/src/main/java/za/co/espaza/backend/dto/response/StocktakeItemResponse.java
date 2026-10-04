package za.co.espaza.backend.dto.response;

import za.co.espaza.backend.entity.StocktakeItem;

import java.util.UUID;

public record StocktakeItemResponse(
        UUID stocktakeItemId,
        UUID productId,
        String productName,
        int systemQuantity,
        Integer countedQuantity,
        Integer discrepancy,
        boolean adjusted
) {

    public static StocktakeItemResponse from(StocktakeItem item) {
        return from(item, null);
    }

    public static StocktakeItemResponse from(StocktakeItem item, String productName) {
        return new StocktakeItemResponse(
                item.getStocktakeItemId(),
                item.getProductId(),
                productName,
                item.getSystemQuantity(),
                item.getCountedQuantity(),
                item.getDiscrepancy(),
                item.isAdjusted()
        );
    }
}
