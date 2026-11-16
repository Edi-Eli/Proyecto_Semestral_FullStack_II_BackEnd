package com.ferreteria.maestros.pedido.dto;

import com.ferreteria.maestros.pedido.entity.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(

        @NotNull(message = "Debe indicar el nuevo estado.")
        EstadoPedido estado
) {
}