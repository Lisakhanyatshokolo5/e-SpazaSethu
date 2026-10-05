package za.co.espaza.backend.dto.response;

import java.math.BigDecimal;

public record SalesSummaryResponse(BigDecimal totalAmount, BigDecimal totalProfit) {
}
