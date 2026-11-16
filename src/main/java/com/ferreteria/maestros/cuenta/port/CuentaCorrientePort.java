package com.ferreteria.maestros.cuenta.port;

import java.math.BigDecimal;

public interface CuentaCorrientePort {

    boolean validarYRegistrarCargo(Long clienteId, BigDecimal monto, Long pedidoId);
}