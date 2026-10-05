package za.co.espaza.backend.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.espaza.backend.dto.response.SaleResponse;
import za.co.espaza.backend.entity.Sale;
import za.co.espaza.backend.enums.SaleStatus;
import za.co.espaza.backend.exception.BusinessRuleException;
import za.co.espaza.backend.exception.EntityNotFoundException;
import za.co.espaza.backend.repository.SaleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Sales history and cancellation (Backend Issue 13).
 *
 * <p>Creating a sale (POST /sales) belongs to Backend Issue 5 / Samukelo and is
 * deliberately not implemented here.</p>
 */
@Service
public class SaleService {

    private final SaleRepository saleRepository;

    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Transactional(readOnly = true)
    public SaleResponse getSaleById(UUID id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sale not found: " + id));
        return toResponse(sale);
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> getSales(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        return saleRepository.findBySaleDateTimeBetween(start, end).stream()
                .sorted(Comparator.comparing(Sale::getSaleDateTime).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SaleResponse cancelSale(UUID id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sale not found: " + id));

        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new BusinessRuleException("Sale is already cancelled");
        }

        // TODO Phase 2: reverse stock on cancellation.
        // The MVP does NOT restore deducted stock when a sale is cancelled -
        // this is a known limitation.
        sale.cancel();
        saleRepository.save(sale);

        return toResponse(sale);
    }

    private SaleResponse toResponse(Sale sale) {
        // TODO (Backend Issue 2): populate product names from ProductRepository once the
        // Product entity exists. Until then item productName is null.
        return SaleResponse.from(sale);
    }
}