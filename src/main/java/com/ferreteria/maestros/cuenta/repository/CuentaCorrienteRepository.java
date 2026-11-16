package com.ferreteria.maestros.cuenta.repository;

import com.ferreteria.maestros.cuenta.entity.CuentaCorriente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaCorrienteRepository extends JpaRepository<CuentaCorriente, Long> {

    Optional<CuentaCorriente> findByClienteId(Long clienteId);

    boolean existsByClienteId(Long clienteId);
}