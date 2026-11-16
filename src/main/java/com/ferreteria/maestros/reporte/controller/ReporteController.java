package com.ferreteria.maestros.reporte.controller;

import com.ferreteria.maestros.reporte.dto.ProductoVendidoDTO;
import com.ferreteria.maestros.reporte.dto.ResumenDTO;
import com.ferreteria.maestros.reporte.dto.StockReporteDTO;
import com.ferreteria.maestros.reporte.dto.TopClienteDTO;
import com.ferreteria.maestros.reporte.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("@autorizacion.esAdmin()")
@Tag(name = "Reportes", description = "Métricas del negocio (solo Administrador).")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/resumen")
    @Operation(summary = "Resumen general",
            description = "Recaudado, total de pedidos, pedidos por estado, total de productos y usuarios.")
    @ApiResponse(responseCode = "200", description = "Resumen del negocio.")
    public ResumenDTO resumen() {
        return reporteService.resumen();
    }

    @GetMapping("/mas-vendidos")
    @Operation(summary = "Productos más vendidos",
            description = "Top de productos por unidades vendidas. Filtros opcionales ?desde=&hasta= (ISO date-time).")
    @ApiResponse(responseCode = "200", description = "Lista de productos más vendidos.")
    public List<ProductoVendidoDTO> masVendidos(
            @Parameter(description = "Fecha/hora desde (ISO, ej. 2026-01-01T00:00:00)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @Parameter(description = "Fecha/hora hasta (ISO)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(defaultValue = "10") int limite) {
        return reporteService.masVendidos(desde, hasta, limite);
    }

    @GetMapping("/top-clientes")
    @Operation(summary = "Top clientes", description = "Clientes con mayor monto comprado.")
    @ApiResponse(responseCode = "200", description = "Lista de clientes.")
    public List<TopClienteDTO> topClientes(@RequestParam(defaultValue = "10") int limite) {
        return reporteService.topClientes(limite);
    }

    @GetMapping("/stock")
    @Operation(summary = "Reporte de stock",
            description = "Producto con mayor stock, con menor stock y lista de productos críticos.")
    @ApiResponse(responseCode = "200", description = "Reporte de stock.")
    public StockReporteDTO stock() {
        return reporteService.stock();
    }
}
