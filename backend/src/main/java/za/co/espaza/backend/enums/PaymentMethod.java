package za.co.espaza.backend.enums;

/**
 * Tender type used to pay for a sale. Stored as a string in the database
 * (schema: {@code sale.paymentMethod ENUM('CASH','CARD','MOBILE_PAYMENT')}).
 */
public enum PaymentMethod {
    CASH,
    CARD,
    MOBILE_PAYMENT
}
