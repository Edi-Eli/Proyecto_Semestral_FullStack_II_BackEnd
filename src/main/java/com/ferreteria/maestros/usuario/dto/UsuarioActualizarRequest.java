package com.ferreteria.maestros.usuario.dto;

import com.ferreteria.maestros.common.validation.Patrones;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Cuerpo de {@code PUT /api/usuarios/{run}}.
 *
 * <p>El RUN no se puede cambiar (es la clave de ruta). Los campos {@code tipoUsuario},
 * {@code activo} y {@code cuentaCorriente} solo los aplica un Administrador; un usuario
 * actualizando su propio perfil no puede modificarlos.</p>
 */
public record UsuarioActualizarRequest(

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres.")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios.")
        @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres.")
        String apellidos,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Pattern(regexp = Patrones.CORREO_DOMINIO_PERMITIDO, message = Patrones.MENSAJE_CORREO_DOMINIO_PERMITIDO)
        @Size(max = 100, message = "El correo no puede superar los 100 caracteres.")
        String correo,

        String tipoUsuario,

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
        String preferenciaEntrega,

        /** Opcional: si viene, se cambia la contraseña. */
        @Size(min = Patrones.PASSWORD_MIN, max = Patrones.PASSWORD_MAX, message = Patrones.MENSAJE_PASSWORD)
        String password,

        Boolean cuentaCorriente,

        Boolean activo
) {
}
