package com.ferreteria.maestros.producto.dto;

public record SubcategoriaDTO(
        Long id,
        Long categoriaId,
        String categoriaNombre,
        String nombre
) {
}
