package za.co.espaza.backend.dto.response;

import za.co.espaza.backend.entity.Sale;
import za.co.espaza.backend.enums.PaymentMethod;
import za.co.espaza.backend.enums.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SaleResponse(
        UUID saleId,
        UUID userId,
        LocalDateTime saleDateTime,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        SaleStatus status,
        String notes,
        LocalDateTime createdAt,
        List<SaleItemResponse> items
) {

    public static SaleResponse from(Sale sale) {
        return from(sale, Map.of());
    }

    public static SaleResponse from(Sale sale, Map<UUID, String> productNames) {
        List<SaleItemResponse> items = sale.getItems().stream()
                .map(item -> SaleItemResponse.from(item, productNames.get(item.getProductId())))
                .toList();

        return new SaleResponse(
                sale.getSaleId(),
                sale.getUserId(),
                sale.getSaleDateTime(),
                sale.getTotalAmount(),
                sale.getPaymentMethod(),
                sale.getStatus(),
                sale.getNotes(),
                sale.getCreatedAt(),
                items
        );
    }
}
