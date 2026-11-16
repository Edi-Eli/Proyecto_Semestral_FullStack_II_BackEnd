package com.ferreteria.maestros.cuenta.repository;

import com.ferreteria.maestros.cuenta.entity.MovimientoCuenta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoCuentaRepository extends JpaRepository<MovimientoCuenta, Long> {

    List<MovimientoCuenta> findByCuentaIdOrderByFechaDesc(Long cuentaId);
}