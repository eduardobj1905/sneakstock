package com.eduardobernal.sneakstock.stock;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public List<StoreStockResponse> findAvailableByBarcode(String barcode) {
        return stockRepository.findAvailableByBarcode(barcode).stream()
                .map(StoreStockResponse::from)
                .toList();
    }
}