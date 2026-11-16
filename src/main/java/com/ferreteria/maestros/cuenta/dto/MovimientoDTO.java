package com.ferreteria.maestros.cuenta.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoDTO(
        Long id,
        String tipo,
        Long pedidoId,
        String descripcion,
        BigDecimal monto,
        BigDecimal saldoPosterior,
        LocalDateTime fecha
) {
}