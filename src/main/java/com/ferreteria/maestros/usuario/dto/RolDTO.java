package com.ferreteria.maestros.usuario.dto;

/** Catálogo de roles del sistema. */
public record RolDTO(
        Long id,
        String nombre,
        String descripcion
) {
}
