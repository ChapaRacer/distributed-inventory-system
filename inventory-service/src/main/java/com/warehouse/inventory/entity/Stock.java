package com.warehouse.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "stock",
    schema = "inventory",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_stock_product_warehouse",
        columnNames = {"product_id", "warehouse_id"}
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "low_stock_threshold", nullable = false)
    @Builder.Default
    private Integer lowStockThreshold = 10;

    @Column(name = "upddated_at")
    private LocalDateTime updatedAt;

    public void deduct(int amount){
        if (this.quantity < amount){
            throw new IllegalStateException(
                String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                    product.getSku(), this.quantity, amount)
            );
        }
        this.quantity -= amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void add(int amount) {
        this.quantity += amount;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isBelowThreshold(){
        return this.quantity <= this.lowStockThreshold;
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}
