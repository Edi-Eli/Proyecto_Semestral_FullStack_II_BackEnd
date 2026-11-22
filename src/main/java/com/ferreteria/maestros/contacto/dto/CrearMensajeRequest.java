package com.ferreteria.maestros.contacto.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de {@code POST /api/contactos} (público). */
public record CrearMensajeRequest(

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
        String nombre,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 100, message = "El correo no puede superar los 100 caracteres.")
        String correo,

        @NotBlank(message = "El mensaje es obligatorio.")
        @Size(max = 500, message = "El mensaje no puede superar los 500 caracteres.")
        String mensaje
) {
}
