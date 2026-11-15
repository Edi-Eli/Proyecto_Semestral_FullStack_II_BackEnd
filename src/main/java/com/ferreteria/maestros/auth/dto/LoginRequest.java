package com.ferreteria.maestros.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de {@code POST /api/auth/login}.
 * El front envía {@code { correo, password }}.
 */
public record LoginRequest(

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria.")
        String password
) {
}
