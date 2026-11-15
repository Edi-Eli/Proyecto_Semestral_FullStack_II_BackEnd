package com.ferreteria.maestros.auth.dto;

/**
 * Subconjunto del usuario que viaja al front tras el login (nunca la contraseña).
 * El front ({@code src/api/auth.js}) persiste solo {@code run, nombre, correo, tipoUsuario}.
 */
public record UsuarioSesionDTO(
        Long id,
        String run,
        String nombre,
        String apellidos,
        String correo,
        String tipoUsuario
) {
}
