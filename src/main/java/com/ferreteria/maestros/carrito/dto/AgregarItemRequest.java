package com.ferreteria.maestros.carrito.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AgregarItemRequest(

        @NotNull(message = "Debe indicar el producto.")
        Long productoId,

        @NotNull(message = "Debe indicar la cantidad.")
        @Min(value = 1, message = "La cantidad debe ser al menos 1.")
        Integer cantidad
) {
}
