package com.ferreteria.maestros.cuenta.dto;

import java.math.BigDecimal;

public record CuentaResumenDTO(
        Long id,
        Long clienteId,
        String clienteNombre,
        BigDecimal limiteCredito,
        BigDecimal saldoActual,
        BigDecimal disponible,
        boolean activa
) {
}