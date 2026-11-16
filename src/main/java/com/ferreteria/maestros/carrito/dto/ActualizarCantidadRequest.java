package com.ferreteria.maestros.carrito.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ActualizarCantidadRequest(

        @NotNull(message = "Debe indicar la cantidad.")
        @Min(value = 0, message = "La cantidad no puede ser negativa (0 elimina el item).")
        Integer cantidad
) {
}
