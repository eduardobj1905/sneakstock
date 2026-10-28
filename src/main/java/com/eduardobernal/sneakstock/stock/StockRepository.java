package com.eduardobernal.sneakstock.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    @Query("""
            select s from Stock s
            join fetch s.store st
            where s.variant.barcode = :barcode
              and st.active = true
              and s.quantity > 0
            order by s.quantity desc
            """)
    List<Stock> findAvailableByBarcode(String barcode);

    Optional<Stock> findByVariantBarcodeAndStoreStoreNumber(String barcode, String storeNumber);
}