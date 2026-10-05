package za.co.espaza.backend.dto.request;

import java.util.UUID;

public record StocktakeCountRequest(UUID stocktakeItemId, Integer countedQuantity) {
}
