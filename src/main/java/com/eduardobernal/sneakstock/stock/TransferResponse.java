package com.eduardobernal.sneakstock.stock;

import com.eduardobernal.sneakstock.catalog.VariantResponse;
import com.eduardobernal.sneakstock.store.Store;

/**
 * Un traspaso visto desde la tienda que lo va a recibir.
 */
public record TransferResponse(
        Long transferId,
        String originStoreNumber,
        String originStoreName,
        String destinationStoreNumber,
        String destinationStoreName,
        VariantResponse variant,
        int sentUnits,
        boolean received
) {

    public static TransferResponse from(StockMovement transferOut, boolean received) {
        Store origin = transferOut.getStock().getStore();
        Store destination = transferOut.getDestinationStore();
        return new TransferResponse(
                transferOut.getId(),
                origin.getStoreNumber(),
                origin.getName(),
                destination.getStoreNumber(),
                destination.getName(),
                VariantResponse.from(transferOut.getStock().getVariant()),
                -transferOut.getQuantity(),
                received
        );
    }
}
