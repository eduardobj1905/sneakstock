package com.eduardobernal.sneakstock.stock;

public record TransferRequest(
        String barcode,
        String originStoreNumber,
        String destinationStoreNumber,
        int units
) {
}