package com.ferreteria.maestros.contacto.dto;

import com.ferreteria.maestros.contacto.entity.EstadoMensaje;
import jakarta.validation.constraints.NotNull;

/** Cuerpo de {@code PATCH /api/contactos/{id}/estado}. */
public record CambiarEstadoMensajeRequest(
        @NotNull(message = "El estado es obligatorio.")
        EstadoMensaje estado
) {
}
