package com.eduardobernal.sneakstock.stock;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional(readOnly = true)
    public List<StoreStockResponse> findAvailableByBarcode(String barcode) {
        return stockRepository.findAvailableByBarcode(barcode).stream()
                .map(StoreStockResponse::from)
                .toList();
    }
    @Transactional
    public void sell(String barcode, String storeNumber, int units) {
        Stock stock = stockRepository.findByVariantBarcodeAndStoreStoreNumber(barcode, storeNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La tienda " + storeNumber + " no tiene la talla " + barcode));

        if (!stock.getStore().isActive()) {
            throw new IllegalStateException("La tienda " + storeNumber + " está cerrada");
        }

        stock.remove(units);

        stockMovementRepository.save(new StockMovement(stock, MovementType.SALE, -units, null));
    }
}