package com.ferreteria.maestros.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record PedidoResumenDTO(
        Long id,
        String codigo,
        String nro,
        LocalDateTime fecha,
        String estado,
        String tipoEntrega,
        String metodoPago,
        BigDecimal total
) {
}
