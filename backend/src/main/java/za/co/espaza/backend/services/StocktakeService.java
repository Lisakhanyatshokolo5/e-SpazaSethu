package za.co.espaza.backend.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.espaza.backend.dto.request.StocktakeCountRequest;
import za.co.espaza.backend.dto.response.StocktakeResponse;
import za.co.espaza.backend.entity.Stocktake;
import za.co.espaza.backend.entity.StocktakeItem;
import za.co.espaza.backend.enums.StocktakeStatus;
import za.co.espaza.backend.exception.BusinessRuleException;
import za.co.espaza.backend.exception.EntityNotFoundException;
import za.co.espaza.backend.repository.StocktakeRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Stocktake business logic (Backend Issue 16).
 *
 * <p><strong>Partially implemented.</strong> {@code submitCounts}, {@code getStocktakesForUser}
 * and {@code getStocktakeById} work with the entities that exist today. {@code startStocktake}
 * and {@code applyAdjustments} are blocked until the missing dependency entities land:</p>
 * <ul>
 *   <li>{@code Product} (Backend Issue 2) — to snapshot active products / adjust stock</li>
 *   <li>{@code StockMovement} + its type enum (Backend Issues 9/12) — to record corrections</li>
 *   <li>{@code User} (Backend Issue 1) — only if the {@code conductedBy} FK must become an association</li>
 * </ul>
 */
@Service
public class StocktakeService {

    private final StocktakeRepository stocktakeRepository;

    public StocktakeService(StocktakeRepository stocktakeRepository) {
        this.stocktakeRepository = stocktakeRepository;
    }

    /**
     * Starts a new stocktake.
     *
     * <p>BLOCKED: creating one {@link StocktakeItem} per active product requires the
     * {@code Product} entity ({@code productRepository.findByActiveTrue()} and
     * {@code product.getStockQuantity()} / {@code product.getProductId()}) from Backend Issue 2.</p>
     */
    @Transactional
    public StocktakeResponse startStocktake(UUID userId) {
        stocktakeRepository.findByStatusAndConductedBy(StocktakeStatus.IN_PROGRESS, userId)
                .ifPresent(existing -> {
                    throw new BusinessRuleException("A stocktake is already in progress");
                });

        throw new UnsupportedOperationException(
                "startStocktake is blocked: the Product entity (Backend Issue 2) is required to "
                        + "snapshot active products and their system quantities");
    }

    /** Records physical counts and derives each item's discrepancy. */
    @Transactional
    public StocktakeResponse submitCounts(UUID stocktakeId, List<StocktakeCountRequest> counts) {
        Stocktake stocktake = getInProgressStocktake(stocktakeId);

        Map<UUID, StocktakeItem> itemsById = stocktake.getItems().stream()
                .collect(Collectors.toMap(StocktakeItem::getStocktakeItemId, item -> item));

        for (StocktakeCountRequest count : counts) {
            if (count.stocktakeItemId() == null) {
                throw new BusinessRuleException("Each count must reference a stocktake item");
            }
            StocktakeItem item = itemsById.get(count.stocktakeItemId());
            if (item == null) {
                throw new EntityNotFoundException("Stocktake item not found: " + count.stocktakeItemId());
            }
            // A null countedQuantity means the item was skipped; discrepancy is cleared.
            item.setCountedQuantity(count.countedQuantity());
        }

        stocktakeRepository.save(stocktake);
        return toResponse(stocktake);
    }

    /**
     * Applies the counted discrepancies and completes the stocktake.
     *
     * <p>BLOCKED: applying requires the {@code Product} entity to change
     * {@code product.stockQuantity} (Backend Issue 2) and a {@code StockMovement}
     * of type {@code STOCKTAKE_CORRECTION} (Backend Issues 9/12).</p>
     */
    @Transactional
    public StocktakeResponse applyAdjustments(UUID stocktakeId, UUID userId) {
        getInProgressStocktake(stocktakeId);

        throw new UnsupportedOperationException(
                "applyAdjustments is blocked: Product (Backend Issue 2) is required to update stock "
                        + "and StockMovement (Backend Issues 9/12) is required to record corrections");
    }

    @Transactional(readOnly = true)
    public List<StocktakeResponse> getStocktakesForUser(UUID userId) {
        return stocktakeRepository.findByConductedByOrderByStartedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StocktakeResponse getStocktakeById(UUID id) {
        Stocktake stocktake = stocktakeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Stocktake not found: " + id));
        return toResponse(stocktake);
    }

    private Stocktake getInProgressStocktake(UUID stocktakeId) {
        Stocktake stocktake = stocktakeRepository.findById(stocktakeId)
                .orElseThrow(() -> new EntityNotFoundException("Stocktake not found: " + stocktakeId));

        if (stocktake.getStatus() != StocktakeStatus.IN_PROGRESS) {
            throw new BusinessRuleException("Stocktake is not in progress");
        }
        return stocktake;
    }

    private StocktakeResponse toResponse(Stocktake stocktake) {
        // TODO (Backend Issue 2): populate product names from ProductRepository once the
        // Product entity exists. Until then item productName is null.
        return StocktakeResponse.from(stocktake);
    }
}