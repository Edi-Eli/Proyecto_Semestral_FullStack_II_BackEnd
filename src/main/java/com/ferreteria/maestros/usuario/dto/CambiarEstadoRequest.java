package com.ferreteria.maestros.usuario.dto;

import jakarta.validation.constraints.NotNull;

/** Cuerpo de {@code PATCH /api/usuarios/{run}/estado}. */
public record CambiarEstadoRequest(
        @NotNull(message = "El campo 'activo' es obligatorio.")
        Boolean activo
) {
}
