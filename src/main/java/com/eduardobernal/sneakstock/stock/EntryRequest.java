package com.eduardobernal.sneakstock.stock;

public record EntryRequest(
        String barcode,
        String storeNumber,
        int units
) {
}