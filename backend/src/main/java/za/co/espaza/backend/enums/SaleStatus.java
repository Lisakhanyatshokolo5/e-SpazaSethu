package za.co.espaza.backend.enums;

/**
 * Lifecycle of a sale. Stored as a string in the database
 * (schema: {@code sale.status ENUM('PENDING','COMPLETED','CANCELLED')}).
 */
public enum SaleStatus {
    PENDING,
    COMPLETED,
    CANCELLED
}
