package za.co.espaza.backend.dto.response;

import java.math.BigDecimal;

public record DashboardReportResponse(
        BigDecimal totalSales,
        BigDecimal totalProfit,
        long transactionCount
) {
}
