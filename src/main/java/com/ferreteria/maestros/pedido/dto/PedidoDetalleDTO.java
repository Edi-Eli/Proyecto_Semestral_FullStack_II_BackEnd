package com.ferreteria.maestros.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoDetalleDTO(
        Long id,
        String codigo,
        String nro,
        LocalDateTime fecha,
        String estado,
        String tipoEntrega,
        String metodoPago,
        BigDecimal total,
        Long usuarioId,
        ClienteDTO cliente,
        DireccionDTO direccion,
        List<PedidoItemDTO> items
) {
}