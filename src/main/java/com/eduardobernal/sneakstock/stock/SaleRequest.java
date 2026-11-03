package com.eduardobernal.sneakstock.stock;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SaleRequest(
        @NotBlank(message = "El código de barras es obligatorio")
        String barcode,

        @NotBlank(message = "El número de tienda es obligatorio")
        String storeNumber,

        @NotNull(message = "Las unidades son obligatorias")
        @Positive(message = "Las unidades deben ser mayores que 0")
        Integer units
) {
}
