package za.co.espaza.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A single line item within a {@link Sale} (table {@code sale_item}).
 *
 * <p>Backend Issue 3. There is deliberately no {@code @PrePersist}: the
 * {@code saleItemId} UUID is assigned by the sale service.</p>
 *
 * <p>The {@code product} association cannot be mapped yet — Backend Issue 2
 * must create the {@code Product} entity first. Until then the foreign key is
 * stored as the raw {@code productId} column. When {@code Product} lands, add
 * {@code @ManyToOne(fetch = LAZY)} with
 * {@code @JoinColumn(name = "productId", insertable = false, updatable = false)}
 * alongside the writable {@code productId} field.</p>
 */
@Entity
@Table(name = "sale_item")
public class SaleItem {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "saleItemId", length = 36, nullable = false, updatable = false)
    private UUID saleItemId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "saleId", nullable = false)
    private Sale sale;

    /**
     * Read-only view of the owning sale's foreign key. It exists alongside the
     * {@link #sale} association so the derived query {@code findBySaleId(...)}
     * resolves directly to this column without needing a join. Only the
     * association writes it.
     */
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "saleId", length = 36, nullable = false, insertable = false, updatable = false)
    private UUID saleId;

    // TODO (Backend Issue 2): add the lazy @ManyToOne Product association here.
    @Column(name = "productId", length = 36, nullable = false)
    private String productId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unitPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /** Required by JPA. */
    protected SaleItem() {
    }

    public SaleItem(String productId, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = calculateSubtotal();
    }

    /** Set by the sale service before persisting (no {@code @PrePersist}). */
    public void setSaleItemId(UUID saleItemId) {
        this.saleItemId = saleItemId;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public BigDecimal calculateSubtotal() {
        this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        return this.subtotal;
    }

    public UUID getSaleItemId() {
        return saleItemId;
    }

    public UUID getSaleId() {
        return sale != null ? sale.getSaleId() : saleId;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
