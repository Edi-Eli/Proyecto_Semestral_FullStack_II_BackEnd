package com.ferreteria.maestros.pedido.dto;

import com.ferreteria.maestros.pedido.entity.MetodoPago;
import com.ferreteria.maestros.pedido.entity.TipoEntrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CheckoutRequest(

        @NotEmpty(message = "El pedido debe tener al menos un producto.")
        @Valid
        List<ItemPedidoRequest> items,

        @NotNull(message = "Debe indicar la dirección.")
        @Valid
        DireccionRequest direccion,

        @NotNull(message = "Debe indicar el tipo de entrega (RETIRO o DESPACHO).")
        TipoEntrega tipoEntrega,

        @NotNull(message = "Debe indicar el método de pago (CONTADO o CUENTA_CORRIENTE).")
        MetodoPago metodoPago
) {
}