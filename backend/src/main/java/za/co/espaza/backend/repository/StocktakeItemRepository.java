package za.co.espaza.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.espaza.backend.entity.StocktakeItem;

import java.util.List;
import java.util.UUID;

@Repository
public interface StocktakeItemRepository extends JpaRepository<StocktakeItem, UUID> {

    // All items captured in a stocktake. Resolves to StocktakeItem.stocktakeId,
    // the read-only column mapped alongside the owning "stocktake" association.
    List<StocktakeItem> findByStocktakeId(UUID stocktakeId);

    // Only the items with a non-zero discrepancy (the items needing a correction).
    // NULL discrepancies are excluded by SQL comparison semantics.
    List<StocktakeItem> findByStocktakeIdAndDiscrepancyNot(UUID stocktakeId, Integer discrepancy);
}
