package za.co.espaza.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.espaza.backend.entity.SaleItem;

import java.util.List;
import java.util.UUID;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {

    // Line items for a sale. Resolves to SaleItem.saleId, the read-only column
    // mapped alongside the owning "sale" association.
    List<SaleItem> findBySaleId(UUID saleId);
}
