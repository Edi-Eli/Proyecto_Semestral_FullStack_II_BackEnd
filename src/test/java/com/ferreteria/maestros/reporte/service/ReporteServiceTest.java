// Reportes (ReporteService) — realizado por: Sebas
package com.ferreteria.maestros.reporte.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ferreteria.maestros.producto.dto.ProductoDTO;
import com.ferreteria.maestros.producto.entity.Producto;
import com.ferreteria.maestros.producto.mapper.ProductoMapper;
import com.ferreteria.maestros.producto.repository.ProductoRepository;
import com.ferreteria.maestros.reporte.dto.ResumenDTO;
import com.ferreteria.maestros.reporte.dto.StockReporteDTO;
import com.ferreteria.maestros.reporte.repository.ReporteVentasQuery;
import com.ferreteria.maestros.usuario.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteVentasQuery reporteVentasQuery;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProductoMapper productoMapper;

    @InjectMocks
    private ReporteService reporteService;

    private Producto producto(String nombre, int stock) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setStock(stock);
        return producto;
    }

    private ProductoDTO dto(String nombre, int stock) {
        return new ProductoDTO(1L, "COD", nombre, "desc", "Marca", "Unidad",
                new BigDecimal("1000"), null, false, stock, 5, "/img.png", 1L, "Cat", 1L, "Sub", true);
    }

    @Test
    void resumenCombinaVentasConConteosDeProductosYUsuarios() {
        when(reporteVentasQuery.recaudadoTotal()).thenReturn(new BigDecimal("15000"));
        when(reporteVentasQuery.totalPedidos()).thenReturn(3L);
        when(reporteVentasQuery.conteoPorEstado()).thenReturn(Map.of("PAGADA", 2L, "PENDIENTE", 1L));
        when(productoRepository.count()).thenReturn(18L);
        when(usuarioRepository.count()).thenReturn(5L);

        ResumenDTO resumen = reporteService.resumen();

        assertEquals(new BigDecimal("15000"), resumen.recaudado());
        assertEquals(3L, resumen.totalPedidos());
        assertEquals(18L, resumen.totalProductos());
        assertEquals(5L, resumen.totalUsuarios());
        assertEquals(2L, resumen.pedidosPorEstado().get("PAGADA"));
    }

    @Test
    void stockDevuelveMayorMenorYCriticos() {
        Producto mayor = producto("Mayor", 100);
        Producto menor = producto("Menor", 1);
        when(productoRepository.findTopByActivoTrueOrderByStockDesc()).thenReturn(Optional.of(mayor));
        when(productoRepository.findTopByActivoTrueOrderByStockAsc()).thenReturn(Optional.of(menor));
        when(productoRepository.findAll(ArgumentMatchers.<Specification<Producto>>any())).thenReturn(List.of(mayor));
        when(productoMapper.aDTO(mayor)).thenReturn(dto("Mayor", 100));
        when(productoMapper.aDTO(menor)).thenReturn(dto("Menor", 1));
        when(productoMapper.aDTOs(anyList())).thenReturn(List.of(dto("Mayor", 100)));

        StockReporteDTO reporte = reporteService.stock();

        assertEquals("Mayor", reporte.mayorStock().nombre());
        assertEquals("Menor", reporte.menorStock().nombre());
        assertEquals(1, reporte.criticos().size());
    }
}
