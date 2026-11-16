package com.ferreteria.maestros.carrito.dto;

import java.math.BigDecimal;

public record CarritoItemDTO(
        Long id,
        Long productoId,
        String nombre,
        BigDecimal precio,
        String imagen,
        Integer cantidad,
        Integer stock
) {
}
