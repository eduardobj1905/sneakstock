package com.eduardobernal.sneakstock.stock;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferRequest(
        @NotBlank(message = "El código de barras es obligatorio")
        String barcode,

        @NotBlank(message = "La tienda de origen es obligatoria")
        String originStoreNumber,

        @NotBlank(message = "La tienda de destino es obligatoria")
        String destinationStoreNumber,

        @NotNull(message = "Las unidades son obligatorias")
        @Positive(message = "Las unidades deben ser mayores que 0")
        Integer units
) {
}
