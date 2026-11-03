package com.eduardobernal.sneakstock.stock;

public record SaleRequest(
        String barcode,
        String storeNumber,
        int units
) {
}