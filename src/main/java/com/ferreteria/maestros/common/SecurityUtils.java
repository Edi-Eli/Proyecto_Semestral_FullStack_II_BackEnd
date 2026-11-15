package com.ferreteria.maestros.common;

import com.ferreteria.maestros.common.util.RutUtils;
import com.ferreteria.maestros.usuario.entity.Usuario;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Acceso al usuario autenticado de la petición en curso.
 *
 * <p>El {@code JwtAuthenticationFilter} deja la entidad {@link Usuario} como {@code principal}
 * de la {@link Authentication}. Estas utilidades centralizan su lectura para no repetir
 * casts por todo el codigo.</p>
 */
public final class SecurityUtils {

    private static final String ROL_ADMINISTRADOR = "administrador";

    private SecurityUtils() {
    }

    /** Devuelve el usuario autenticado, si lo hay. */
    public static Optional<Usuario> usuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Usuario usuario) {
            return Optional.of(usuario);
        }
        return Optional.empty();
    }

    /** {@code true} si el usuario autenticado tiene rol Administrador. */
    public static boolean esAdmin() {
        return usuarioActual()
                .map(usuario -> ROL_ADMINISTRADOR.equalsIgnoreCase(usuario.getRol().getNombre()))
                .orElse(false);
    }

    /** {@code true} si el usuario autenticado es Administrador o es el dueño del RUN indicado. */
    public static boolean esAdminODuenio(String run) {
        String runNormalizado = RutUtils.normalizar(run);
        return usuarioActual()
                .map(usuario -> ROL_ADMINISTRADOR.equalsIgnoreCase(usuario.getRol().getNombre())
                        || usuario.getRun().equalsIgnoreCase(runNormalizado))
                .orElse(false);
    }
}
