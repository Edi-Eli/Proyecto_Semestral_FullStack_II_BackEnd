package com.ferreteria.maestros.cuenta.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;


public record AbonoRequest(

        @NotNull(message = "El monto del abono es obligatorio.")
        @DecimalMin(value = "0.01", message = "El abono debe ser mayor que 0.")
        BigDecimal monto
) {
}