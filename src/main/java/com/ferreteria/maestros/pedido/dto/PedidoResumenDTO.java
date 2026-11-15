package com.ferreteria.maestros.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resumen de un pedido para listados (historial de compras de un usuario, panel, etc.).
 *
 * <p><b>Contrato entre módulos</b> (doc 04 §3): lo produce el módulo {@code pedido} (Zeus)
 * a través de {@link com.ferreteria.maestros.pedido.port.PedidoQueryPort} y lo consume el
 * módulo {@code usuario} (Edi). No debe cambiarse sin avisar al equipo.</p>
 */
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
