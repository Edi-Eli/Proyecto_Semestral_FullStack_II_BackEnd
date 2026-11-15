package com.ferreteria.maestros.usuario.repository;

import com.ferreteria.maestros.usuario.entity.Usuario;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/**
 * Especificaciones (Criteria API) para filtrar usuarios.
 *
 * <p>Se construyen <b>solo</b> los predicados de los filtros presentes. Esto evita el patrón
 * {@code :parametro IS NULL} en JPQL, que en PostgreSQL falla porque un parámetro nulo se envía
 * sin tipo y funciones como {@code lower(?)} no pueden resolverse ({@code lower(bytea) does not exist}).</p>
 */
public final class UsuarioSpecifications {

    private UsuarioSpecifications() {
    }

    /** Filtra por nombre de rol (insensible a mayúsculas). */
    public static Specification<Usuario> conRol(String rol) {
        String rolNormalizado = rol.toLowerCase(Locale.ROOT);
        return (root, query, cb) -> cb.equal(cb.lower(root.get("rol").get("nombre")), rolNormalizado);
    }

    /** Filtra por estado activo/inactivo. */
    public static Specification<Usuario> conActivo(Boolean activo) {
        return (root, query, cb) -> cb.equal(root.get("activo"), activo);
    }

    /** Busca un texto libre en nombre, apellidos, correo o RUN. */
    public static Specification<Usuario> conTexto(String texto) {
        String patron = "%" + texto.toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombre")), patron),
                cb.like(cb.lower(root.get("apellidos")), patron),
                cb.like(cb.lower(root.get("correo")), patron),
                cb.like(cb.lower(root.get("run")), patron));
    }
}
