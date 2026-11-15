package com.ferreteria.maestros.auth.dto;

/**
 * Respuesta de {@code GET /api/auth/sesion} (y su alias {@code /api/auth/validar}).
 * El front acepta {@code { usuario: ... }}.
 */
public record SesionResponse(UsuarioSesionDTO usuario) {
}
