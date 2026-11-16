package com.ferreteria.maestros.carrito.repository;

import com.ferreteria.maestros.carrito.entity.Carrito;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByUsuarioId(Long usuarioId);
}
