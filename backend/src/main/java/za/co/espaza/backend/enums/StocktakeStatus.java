package za.co.espaza.backend.enums;

/**
 * Lifecycle of a stocktake. Stored as a string in the database
 * (schema: {@code stocktake.status ENUM('IN_PROGRESS','COMPLETED','CANCELLED')}).
 */
public enum StocktakeStatus {
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
