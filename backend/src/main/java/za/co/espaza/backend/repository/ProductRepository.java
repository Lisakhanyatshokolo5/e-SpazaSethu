package za.co.espaza.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.stereotype.Repository;
import za.co.espaza.backend.domain.Product;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    // All products that are still active (used when starting a stocktake)
    List<Product> findByActiveTrue();
=======
import org.springframework.data.jpa.repository.Query;
import za.co.espaza.backend.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository  extends JpaRepository<Product, String> {

    Optional<Product> findByBarcode(String barcode);
    List<Product> findByIsActiveTrue();
    List<Product> findByIsActiveTrueAndNameContainingIgnoreCase(String name);
    List<Product> findByIsActiveTrueAndCategory_CategoryId(String categoryId);
    List<Product> findByIsActiveTrueAndNameContainingIgnoreCaseAndCategory_CategoryId(String name, String categoryId);
    @Query("""
            SELECT p FROM Product p
            WHERE p.isActive = true
              AND p.lowStockThreshold IS NOT NULL
              AND p.stockQuantity <= p.lowStockThreshold
            ORDER BY p.stockQuantity ASC, p.name ASC
            """)
    List<Product> findLowStockProducts();

>>>>>>> 980971711850d9a8eea62261251ae384a4344f17
}
