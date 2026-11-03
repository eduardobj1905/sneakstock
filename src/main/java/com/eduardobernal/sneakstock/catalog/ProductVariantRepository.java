package com.eduardobernal.sneakstock.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findByBarcode(String barcode);
}