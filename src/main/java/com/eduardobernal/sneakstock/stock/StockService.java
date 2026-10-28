package com.eduardobernal.sneakstock.stock;

import com.eduardobernal.sneakstock.catalog.ProductVariant;
import com.eduardobernal.sneakstock.catalog.ProductVariantRepository;
import com.eduardobernal.sneakstock.store.Store;
import com.eduardobernal.sneakstock.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StoreRepository storeRepository;

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
    @Transactional
    public void receive(String barcode, String storeNumber, int units) {
        Stock stock = stockRepository.findByVariantBarcodeAndStoreStoreNumber(barcode, storeNumber)
                .orElseGet(() -> createStock(barcode, storeNumber));

        if (!stock.getStore().isActive()) {
            throw new IllegalStateException("La tienda " + storeNumber + " está cerrada");
        }

        stock.add(units);

        stockMovementRepository.save(new StockMovement(stock, MovementType.ENTRY, units, null));
    }

    private Stock createStock(String barcode, String storeNumber) {
        ProductVariant variant = productVariantRepository.findByBarcode(barcode)
                .orElseThrow(() -> new IllegalArgumentException("No existe ninguna talla con el código " + barcode));
        Store store = storeRepository.findByStoreNumber(storeNumber)
                .orElseThrow(() -> new IllegalArgumentException("No existe la tienda " + storeNumber));

        return stockRepository.save(new Stock(variant, store));
    }
}