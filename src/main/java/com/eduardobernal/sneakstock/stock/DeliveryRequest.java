package com.eduardobernal.sneakstock.stock;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Recepción de un albarán completo: todas las zapatillas escaneadas de un envío.
 */
public record DeliveryRequest(
        @NotBlank(message = "El número de albarán es obligatorio")
        @Size(max = 30, message = "El número de albarán no puede tener más de 30 caracteres")
        String deliveryNote,

        @NotBlank(message = "El número de tienda es obligatorio")
        String storeNumber,

        @NotEmpty(message = "El albarán tiene que tener al menos una zapatilla")
        List<@Valid Line> lines
) {

    public record Line(
            @NotBlank(message = "El código de barras es obligatorio")
            String barcode,

            @NotNull(message = "Las unidades son obligatorias")
            @Positive(message = "Las unidades deben ser mayores que 0")
            Integer units
    ) {
    }
}
