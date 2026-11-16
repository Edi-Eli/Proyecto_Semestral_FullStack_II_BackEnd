package com.ferreteria.maestros.producto.dto;

import java.math.BigDecimal;

public record ProductoDTO(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        String marca,
        String unidad,
        BigDecimal precio,
        BigDecimal precioOferta,
        boolean enOferta,
        Integer stock,
        Integer stockCritico,
        String imagen,
        Long categoriaId,
        String categoriaNombre,
        Long subcategoriaId,
        String subcategoriaNombre,
        boolean activo
) {
}
