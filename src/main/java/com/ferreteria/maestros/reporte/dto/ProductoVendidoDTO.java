package com.ferreteria.maestros.reporte.dto;

import java.math.BigDecimal;

public record ProductoVendidoDTO(
        Long productoId,
        String nombre,
        long cantidad,
        BigDecimal total
) {
}
