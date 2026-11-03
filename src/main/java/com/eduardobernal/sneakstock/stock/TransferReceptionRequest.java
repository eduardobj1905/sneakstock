package com.eduardobernal.sneakstock.stock;

public record TransferReceptionRequest(
        String storeNumber,
        int units
) {
}