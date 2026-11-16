package com.ferreteria.maestros.cuenta.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record LimiteRequest(

        @NotNull(message = "El límite de crédito es obligatorio.")
        @DecimalMin(value = "0.0", message = "El límite de crédito no puede ser negativo.")
        BigDecimal limiteCredito
) {
}