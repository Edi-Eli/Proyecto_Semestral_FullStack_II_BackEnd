package com.ferreteria.maestros.reporte.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ResumenDTO(
        BigDecimal recaudado,
        long totalPedidos,
        Map<String, Long> pedidosPorEstado,
        long totalProductos,
        long totalUsuarios
) {
}
