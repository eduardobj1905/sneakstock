package com.eduardobernal.sneakstock.store;

public record StoreResponse(
        String storeNumber,
        String name
) {

    public static StoreResponse from(Store store) {
        return new StoreResponse(store.getStoreNumber(), store.getName());
    }
}
