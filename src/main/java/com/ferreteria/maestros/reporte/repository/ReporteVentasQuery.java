package com.ferreteria.maestros.reporte.repository;

import com.ferreteria.maestros.reporte.dto.ProductoVendidoDTO;
import com.ferreteria.maestros.reporte.dto.TopClienteDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class ReporteVentasQuery {

    private final EntityManager entityManager;

    public ReporteVentasQuery(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public BigDecimal recaudadoTotal() {
        Object resultado = entityManager
                .createNativeQuery("SELECT COALESCE(SUM(total), 0) FROM pedidos WHERE estado <> 'RECHAZADA'")
                .getSingleResult();
        return aBigDecimal(resultado);
    }

    public long totalPedidos() {
        Object resultado = entityManager
                .createNativeQuery("SELECT COUNT(*) FROM pedidos")
                .getSingleResult();
        return ((Number) resultado).longValue();
    }

    public Map<String, Long> conteoPorEstado() {
        @SuppressWarnings("unchecked")
        List<Object[]> filas = entityManager
                .createNativeQuery("SELECT estado, COUNT(*) FROM pedidos GROUP BY estado")
                .getResultList();
        Map<String, Long> conteo = new LinkedHashMap<>();
        for (Object[] fila : filas) {
            conteo.put((String) fila[0], ((Number) fila[1]).longValue());
        }
        return conteo;
    }

    public List<ProductoVendidoDTO> masVendidos(LocalDateTime desde, LocalDateTime hasta, int limite) {
        StringBuilder sql = new StringBuilder(
                "SELECT pi.producto_id, MAX(pi.nombre) AS nombre, SUM(pi.cantidad) AS cantidad, SUM(pi.subtotal) AS total "
                        + "FROM pedido_items pi JOIN pedidos p ON p.id = pi.pedido_id "
                        + "WHERE p.estado <> 'RECHAZADA'");
        if (desde != null) {
            sql.append(" AND p.fecha_creacion >= :desde");
        }
        if (hasta != null) {
            sql.append(" AND p.fecha_creacion <= :hasta");
        }
        sql.append(" GROUP BY pi.producto_id ORDER BY SUM(pi.cantidad) DESC");

        Query query = entityManager.createNativeQuery(sql.toString());
        if (desde != null) {
            query.setParameter("desde", desde);
        }
        if (hasta != null) {
            query.setParameter("hasta", hasta);
        }
        query.setMaxResults(limite);

        @SuppressWarnings("unchecked")
        List<Object[]> filas = query.getResultList();
        return filas.stream()
                .map(fila -> new ProductoVendidoDTO(
                        ((Number) fila[0]).longValue(),
                        (String) fila[1],
                        ((Number) fila[2]).longValue(),
                        aBigDecimal(fila[3])))
                .toList();
    }

    public List<TopClienteDTO> topClientes(int limite) {
        Query query = entityManager.createNativeQuery(
                "SELECT p.usuario_id, u.nombre, u.correo, COUNT(p.id) AS pedidos, SUM(p.total) AS total "
                        + "FROM pedidos p JOIN usuarios u ON u.id = p.usuario_id "
                        + "WHERE p.estado <> 'RECHAZADA' "
                        + "GROUP BY p.usuario_id, u.nombre, u.correo "
                        + "ORDER BY SUM(p.total) DESC");
        query.setMaxResults(limite);

        @SuppressWarnings("unchecked")
        List<Object[]> filas = query.getResultList();
        return filas.stream()
                .map(fila -> new TopClienteDTO(
                        ((Number) fila[0]).longValue(),
                        (String) fila[1],
                        (String) fila[2],
                        ((Number) fila[3]).longValue(),
                        aBigDecimal(fila[4])))
                .toList();
    }

    private BigDecimal aBigDecimal(Object valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(valor.toString());
    }
}
