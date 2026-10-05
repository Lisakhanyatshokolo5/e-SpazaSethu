package za.co.espaza.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.espaza.backend.entity.Stocktake;
import za.co.espaza.backend.enums.StocktakeStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StocktakeRepository extends JpaRepository<Stocktake, UUID> {

    // All stocktakes conducted by a user, most recent first
    List<Stocktake> findByConductedByOrderByStartedAtDesc(UUID conductedBy);

    // Find an in-progress (or otherwise filtered) stocktake for a user
    Optional<Stocktake> findByStatusAndConductedBy(StocktakeStatus status, UUID conductedBy);
}
