package com.ferreteria.maestros.usuario.repository;

import com.ferreteria.maestros.usuario.entity.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(String nombre);

    Optional<Rol> findByNombreIgnoreCase(String nombre);

    boolean existsByNombre(String nombre);
}
