package com.ferreteria.maestros.reporte.service;

import com.ferreteria.maestros.producto.dto.ProductoDTO;
import com.ferreteria.maestros.producto.entity.Producto;
import com.ferreteria.maestros.producto.mapper.ProductoMapper;
import com.ferreteria.maestros.producto.repository.ProductoRepository;
import com.ferreteria.maestros.producto.repository.ProductoSpecifications;
import com.ferreteria.maestros.reporte.dto.ProductoVendidoDTO;
import com.ferreteria.maestros.reporte.dto.ResumenDTO;
import com.ferreteria.maestros.reporte.dto.StockReporteDTO;
import com.ferreteria.maestros.reporte.dto.TopClienteDTO;
import com.ferreteria.maestros.reporte.repository.ReporteVentasQuery;
import com.ferreteria.maestros.usuario.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteService {

    private final ReporteVentasQuery reporteVentasQuery;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoMapper productoMapper;

    public ReporteService(ReporteVentasQuery reporteVentasQuery,
                          ProductoRepository productoRepository,
                          UsuarioRepository usuarioRepository,
                          ProductoMapper productoMapper) {
        this.reporteVentasQuery = reporteVentasQuery;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoMapper = productoMapper;
    }

    @Transactional(readOnly = true)
    public ResumenDTO resumen() {
        return new ResumenDTO(
                reporteVentasQuery.recaudadoTotal(),
                reporteVentasQuery.totalPedidos(),
                reporteVentasQuery.conteoPorEstado(),
                productoRepository.count(),
                usuarioRepository.count());
    }

    @Transactional(readOnly = true)
    public List<ProductoVendidoDTO> masVendidos(LocalDateTime desde, LocalDateTime hasta, int limite) {
        return reporteVentasQuery.masVendidos(desde, hasta, limite);
    }

    @Transactional(readOnly = true)
    public List<TopClienteDTO> topClientes(int limite) {
        return reporteVentasQuery.topClientes(limite);
    }

    @Transactional(readOnly = true)
    public StockReporteDTO stock() {
        ProductoDTO mayor = productoRepository.findTopByActivoTrueOrderByStockDesc()
                .map(productoMapper::aDTO)
                .orElse(null);
        ProductoDTO menor = productoRepository.findTopByActivoTrueOrderByStockAsc()
                .map(productoMapper::aDTO)
                .orElse(null);

        Specification<Producto> criticos = Specification.allOf(
                ProductoSpecifications.conActivo(true),
                ProductoSpecifications.conStockCritico());
        List<ProductoDTO> criticosLista = productoMapper.aDTOs(productoRepository.findAll(criticos));

        return new StockReporteDTO(mayor, menor, criticosLista);
    }
}
