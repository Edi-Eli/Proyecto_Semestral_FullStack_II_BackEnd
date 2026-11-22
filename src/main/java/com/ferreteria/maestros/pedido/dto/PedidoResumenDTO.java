package com.ferreteria.maestros.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Resumen de un pedido para listados.
 *
 * <p>Incluye el cliente, la dirección y los ítems porque el frontend muestra
 * esos datos en las tablas de "Mis pedidos", "Órdenes" e "Historial", y en el
 * modal de boleta. Sin ellos, las vistas intentaban leer propiedades que no
 * existían en el DTO y fallaban en tiempo de ejecución.</p>
 */
public record PedidoResumenDTO(
        Long id,
        String codigo,
        String nro,
        LocalDateTime fecha,
        String estado,
        String tipoEntrega,
        String metodoPago,
        BigDecimal total,
        ClienteDTO cliente,
        DireccionDTO direccion,
        List<PedidoItemDTO> items
) {
}
