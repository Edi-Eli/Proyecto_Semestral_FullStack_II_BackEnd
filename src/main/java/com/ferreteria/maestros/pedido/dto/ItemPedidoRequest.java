package com.ferreteria.maestros.pedido.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemPedidoRequest(

        @NotNull(message = "Debe indicar el producto.")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria.")
        @Min(value = 1, message = "La cantidad debe ser al menos 1.")
        Integer cantidad
) {
}