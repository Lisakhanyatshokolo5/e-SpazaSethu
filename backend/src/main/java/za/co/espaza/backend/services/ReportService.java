package za.co.espaza.backend.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.espaza.backend.dto.response.DashboardReportResponse;
import za.co.espaza.backend.dto.response.SalesSummaryResponse;
import za.co.espaza.backend.entity.Product;
import za.co.espaza.backend.entity.Sale;
import za.co.espaza.backend.entity.SaleItem;
import za.co.espaza.backend.enums.SaleStatus;
import za.co.espaza.backend.exception.BusinessRuleException;
import za.co.espaza.backend.repository.ProductRepository;
import za.co.espaza.backend.repository.SaleRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public ReportService(SaleRepository saleRepository, ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public DashboardReportResponse dashboard() {
        LocalDate today = LocalDate.now();
        List<Sale> sales = completedSales(today, today);
        return new DashboardReportResponse(totalSales(sales), totalProfit(sales), sales.size());
    }

    @Transactional(readOnly = true)
    public SalesSummaryResponse summary(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessRuleException("'from' date cannot be after 'to' date");
        }
        List<Sale> sales = completedSales(from, to);
        return new SalesSummaryResponse(totalSales(sales), totalProfit(sales));
    }

    private List<Sale> completedSales(LocalDate from, LocalDate to) {
        return saleRepository.findBySaleDateTimeBetweenAndStatus(
                from.atStartOfDay(), to.atTime(LocalTime.MAX), SaleStatus.COMPLETED);
    }

    private BigDecimal totalSales(List<Sale> sales) {
        return sales.stream().map(Sale::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalProfit(List<Sale> sales) {
        List<String> ids = sales.stream().flatMap(sale -> sale.getItems().stream())
                .map(SaleItem::getProductId).distinct().toList();
        Map<String, Product> products = productRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        return sales.stream().flatMap(sale -> sale.getItems().stream()).map(item -> {
            Product product = products.get(item.getProductId());
            BigDecimal cost = product == null || product.getCostPrice() == null
                    ? BigDecimal.ZERO
                    : product.getCostPrice();
            return item.getUnitPrice().subtract(cost).multiply(BigDecimal.valueOf(item.getQuantity()));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
