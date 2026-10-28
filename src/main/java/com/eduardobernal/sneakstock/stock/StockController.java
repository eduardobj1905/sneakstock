package com.eduardobernal.sneakstock.stock;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public List<StoreStockResponse> findAvailable(@RequestParam String barcode) {
        return stockService.findAvailableByBarcode(barcode);
    }
    @PostMapping("/sales")
    @ResponseStatus(HttpStatus.CREATED)
    public void sell(@RequestBody SaleRequest request) {
        stockService.sell(request.barcode(), request.storeNumber(), request.units());
    }
}