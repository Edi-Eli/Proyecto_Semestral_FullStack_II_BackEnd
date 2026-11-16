package com.ferreteria.maestros.producto.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ActualizarStockRequest(

        @NotNull(message = "La cantidad es obligatoria.")
        @Min(value = 1, message = "La cantidad debe ser al menos 1.")
        Integer cantidad,

        @NotBlank(message = "Debe indicar la acción (AGREGAR o RETIRAR).")
        @Pattern(regexp = "AGREGAR|RETIRAR", message = "La acción debe ser AGREGAR o RETIRAR.")
        String accion
) {
}
