package com.ferreteria.maestros.auth.dto;

import com.ferreteria.maestros.common.validation.Patrones;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Cuerpo de {@code POST /api/auth/registro}. Crea un usuario con rol <b>Cliente</b>.
 * dominios de correo permitidos, contraseña de 4–10 caracteres y RUN chileno.
 */
public record RegistroRequest(

        @NotBlank(message = "El RUN es obligatorio.")
        @Size(max = 20, message = "El RUN no puede superar los 20 caracteres.")
        String run,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Pattern(regexp = Patrones.CORREO_DOMINIO_PERMITIDO, message = Patrones.MENSAJE_CORREO_DOMINIO_PERMITIDO)
        @Size(max = 100, message = "El correo no puede superar los 100 caracteres.")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = Patrones.PASSWORD_MIN, max = Patrones.PASSWORD_MAX, message = Patrones.MENSAJE_PASSWORD)
        String password,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres.")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios.")
        @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres.")
        String apellidos,

        @Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
        LocalDate fechaNacimiento,

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres.")
        String telefono,

        @Size(max = 100, message = "La región no puede superar los 100 caracteres.")
        String region,

        @Size(max = 100, message = "La comuna no puede superar los 100 caracteres.")
        String comuna,

        @Size(max = 300, message = "La dirección no puede superar los 300 caracteres.")
        String direccion,

        @Pattern(regexp = Patrones.PREFERENCIA_ENTREGA, message = Patrones.MENSAJE_PREFERENCIA_ENTREGA)
        String preferenciaEntrega
) {
}
