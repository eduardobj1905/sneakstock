package com.eduardobernal.sneakstock.stock;

import com.eduardobernal.sneakstock.catalog.ProductVariant;
import com.eduardobernal.sneakstock.store.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private int quantity;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Stock(ProductVariant variant, Store store) {
        this.variant = variant;
        this.store = store;
        this.quantity = 0;
    }

    public void remove(int units) {
        if (units <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }
        if (units > quantity) {
            throw new IllegalStateException(
                    "Stock insuficiente: hay " + quantity + " pares y se piden " + units);
        }
        quantity -= units;
    }

    public void add(int units) {
        if (units <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }
        quantity += units;
    }
}