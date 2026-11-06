package com.eduardobernal.sneakstock.stock;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    boolean existsByTransferOutId(Long transferOutId);

    boolean existsByDeliveryNote(String deliveryNote);
}