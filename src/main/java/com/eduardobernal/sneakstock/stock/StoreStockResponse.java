package com.eduardobernal.sneakstock.stock;

import com.eduardobernal.sneakstock.store.Store;

public record StoreStockResponse(
        String storeNumber,
        String storeName,
        String phone,
        int quantity
) {

    public static StoreStockResponse of(Store store, int quantity) {
        return new StoreStockResponse(store.getStoreNumber(), store.getName(), store.getPhone(), quantity);
    }

    public static StoreStockResponse from(Stock stock) {
        return new StoreStockResponse(
                stock.getStore().getStoreNumber(),
                stock.getStore().getName(),
                stock.getStore().getPhone(),
                stock.getQuantity()
        );
    }
}