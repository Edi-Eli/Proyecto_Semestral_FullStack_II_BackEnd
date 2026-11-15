package com.ferreteria.maestros.pedido.port;

import com.ferreteria.maestros.pedido.dto.PedidoResumenDTO;
import java.util.List;

/**
 * Puerto de consulta del módulo <b>pedido</b> hacia otros módulos del monolito.
 *
 * <p><b>Contrato entre módulos</b> (doc 04 §3): el módulo {@code usuario} (Edi) lo usa para el
 * historial de compras; el módulo {@code pedido} (Zeus, Fase 6) lo implementa con
 * {@code PedidoService}. En un monolito la comunicación es por inyección directa de servicios,
 * sin Feign/WebClient.</p>
 *
 * <p>Mientras la implementación no exista (hasta la Fase 6), {@code UsuarioService} lo inyecta
 * de forma opcional y devuelve una lista vacía.</p>
 */
public interface PedidoQueryPort {

    /** Historial de compras de un cliente, más reciente primero. */
    List<PedidoResumenDTO> historialPorUsuario(Long usuarioId);
}
