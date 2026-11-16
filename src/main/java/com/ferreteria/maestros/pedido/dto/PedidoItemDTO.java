package com.ferreteria.maestros.pedido.dto;

import java.math.BigDecimal;

public record PedidoItemDTO(
        Long productoId,
        String nombre,
        BigDecimal precio,
        Integer cantidad,
        BigDecimal subtotal
) {
}