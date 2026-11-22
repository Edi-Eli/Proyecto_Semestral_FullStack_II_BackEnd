package com.ferreteria.maestros.contacto.dto;

import com.ferreteria.maestros.contacto.entity.EstadoMensaje;
import java.time.LocalDateTime;

/** Vista pública de un mensaje de contacto (panel de administración). */
public record MensajeContactoDTO(
        Long id,
        String nombre,
        String correo,
        String mensaje,
        EstadoMensaje estado,
        LocalDateTime fecha
) {
}
