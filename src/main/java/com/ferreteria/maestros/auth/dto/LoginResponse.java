package com.ferreteria.maestros.auth.dto;

/**
 * Respuesta de {@code POST /api/auth/login} y {@code POST /api/auth/registro}.
 * Formato esperado por el front: {@code { token, tipo, usuario }}.
 */
public record LoginResponse(
        String token,
        String tipo,
        UsuarioSesionDTO usuario
) {
}
