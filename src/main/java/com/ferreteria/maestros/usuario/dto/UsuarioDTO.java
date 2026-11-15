package com.ferreteria.maestros.usuario.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Vista pública de un usuario (nunca incluye la contraseña ni su hash).
 * Los nombres de los campos coinciden con los que espera el front (p. ej. {@code tipoUsuario}).
 */
public record UsuarioDTO(
        Long id,
        String run,
        String nombre,
        String apellidos,
        String correo,
        LocalDate fechaNacimiento,
        String telefono,
        String region,
        String comuna,
        String direccion,
        String preferenciaEntrega,
        boolean cuentaCorriente,
        boolean activo,
        String tipoUsuario,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}
