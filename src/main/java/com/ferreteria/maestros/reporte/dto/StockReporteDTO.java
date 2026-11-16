package com.ferreteria.maestros.reporte.dto;

import com.ferreteria.maestros.producto.dto.ProductoDTO;
import java.util.List;

public record StockReporteDTO(
        ProductoDTO mayorStock,
        ProductoDTO menorStock,
        List<ProductoDTO> criticos
) {
}
