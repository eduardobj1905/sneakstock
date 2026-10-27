package com.eduardobernal.sneakstock.stock;

public record StoreStockResponse(
        String storeNumber,
        String storeName,
        String phone,
        int quantity
) {

    public static StoreStockResponse from(Stock stock) {
        return new StoreStockResponse(
                stock.getStore().getStoreNumber(),
                stock.getStore().getName(),
                stock.getStore().getPhone(),
                stock.getQuantity()
        );
    }
}