package com.ferreteria.maestros.reporte.dto;

import java.math.BigDecimal;

public record TopClienteDTO(
        Long usuarioId,
        String nombre,
        String correo,
        long cantidadPedidos,
        BigDecimal total
) {
}
