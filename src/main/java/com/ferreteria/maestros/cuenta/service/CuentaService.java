package com.ferreteria.maestros.cuenta.service;

import com.ferreteria.maestros.common.exception.RecursoDuplicadoException;
import com.ferreteria.maestros.common.exception.RecursoNoEncontradoException;
import com.ferreteria.maestros.common.exception.ReglaNegocioException;
import com.ferreteria.maestros.cuenta.dto.AbrirCuentaRequest;
import com.ferreteria.maestros.cuenta.dto.AbonoRequest;
import com.ferreteria.maestros.cuenta.dto.CuentaResumenDTO;
import com.ferreteria.maestros.cuenta.dto.LimiteRequest;
import com.ferreteria.maestros.cuenta.dto.MovimientoDTO;
import com.ferreteria.maestros.cuenta.entity.CuentaCorriente;
import com.ferreteria.maestros.cuenta.entity.MovimientoCuenta;
import com.ferreteria.maestros.cuenta.entity.TipoMovimiento;
import com.ferreteria.maestros.cuenta.mapper.CuentaMapper;
import com.ferreteria.maestros.cuenta.port.CuentaCorrientePort;
import com.ferreteria.maestros.cuenta.repository.CuentaCorrienteRepository;
import com.ferreteria.maestros.cuenta.repository.MovimientoCuentaRepository;
import com.ferreteria.maestros.usuario.entity.Usuario;
import com.ferreteria.maestros.usuario.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CuentaService implements CuentaCorrientePort {

    private static final Logger log = LoggerFactory.getLogger(CuentaService.class);

    private final CuentaCorrienteRepository cuentaRepository;
    private final MovimientoCuentaRepository movimientoRepository;
    /** Para resolver el cliente al abrir la cuenta (Edi aún no expone un método por id). */
    private final UsuarioRepository usuarioRepository;
    private final CuentaMapper cuentaMapper;

    public CuentaService(CuentaCorrienteRepository cuentaRepository,
                         MovimientoCuentaRepository movimientoRepository,
                         UsuarioRepository usuarioRepository,
                         CuentaMapper cuentaMapper) {
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.cuentaMapper = cuentaMapper;
    }

    @Transactional(readOnly = true)
    public CuentaResumenDTO miCuenta(Long clienteId) {
        return cuentaMapper.aResumen(buscarPorCliente(clienteId));
    }

    @Transactional(readOnly = true)
    public CuentaResumenDTO obtenerCuenta(Long clienteId) {
        return cuentaMapper.aResumen(buscarPorCliente(clienteId));
    }

    @Transactional(readOnly = true)
    public List<MovimientoDTO> listarMovimientos(Long clienteId, LocalDate desde, LocalDate hasta) {
        CuentaCorriente cuenta = buscarPorCliente(clienteId);
        LocalDateTime desdeTs = desde == null ? null : desde.atStartOfDay();
        LocalDateTime hastaTs = hasta == null ? null : hasta.atTime(LocalTime.MAX);

        List<MovimientoCuenta> movimientos = movimientoRepository
                .findByCuentaIdOrderByFechaDesc(cuenta.getId())
                .stream()
                .filter(m -> desdeTs == null || !m.getFecha().isBefore(desdeTs))
                .filter(m -> hastaTs == null || !m.getFecha().isAfter(hastaTs))
                .toList();

        return cuentaMapper.aMovimientoDTOs(movimientos);
    }

    @Transactional
    public CuentaResumenDTO abrirCuenta(AbrirCuentaRequest request) {
        if (cuentaRepository.existsByClienteId(request.clienteId())) {
            throw new RecursoDuplicadoException("El cliente ya tiene una cuenta corriente.");
        }
        Usuario cliente = usuarioRepository.findById(request.clienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un usuario con id " + request.clienteId() + "."));

        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setCliente(cliente);
        cuenta.setLimiteCredito(request.limiteCredito());
        cuenta.setSaldoActual(BigDecimal.ZERO);
        cuenta.setActiva(true);
        CuentaCorriente guardada = cuentaRepository.save(cuenta);

        cliente.setCuentaCorriente(true);
        usuarioRepository.save(cliente);

        log.info("Cuenta corriente abierta para '{}' con límite {}", cliente.getCorreo(), request.limiteCredito());
        return cuentaMapper.aResumen(guardada);
    }

    @Transactional
    public CuentaResumenDTO ajustarLimite(Long clienteId, LimiteRequest request) {
        CuentaCorriente cuenta = buscarPorCliente(clienteId);
        if (request.limiteCredito().compareTo(cuenta.getSaldoActual()) < 0) {
            throw new ReglaNegocioException(
                    "El límite no puede ser menor que el saldo usado (" + cuenta.getSaldoActual() + ").");
        }
        cuenta.setLimiteCredito(request.limiteCredito());
        CuentaCorriente guardada = cuentaRepository.save(cuenta);
        log.info("Límite del cliente {} actualizado a {}", clienteId, request.limiteCredito());
        return cuentaMapper.aResumen(guardada);
    }

    @Transactional
    public CuentaResumenDTO registrarAbono(Long clienteId, AbonoRequest request) {
        CuentaCorriente cuenta = buscarPorCliente(clienteId);
        return registrarMovimiento(cuenta, TipoMovimiento.ABONO, request.monto(), null, "Abono / pago de cuenta");
    }

    @Transactional(readOnly = true)
    public List<CuentaResumenDTO> resumenCobros() {
        return cuentaRepository.findAll().stream()
                .filter(c -> c.getSaldoActual().compareTo(BigDecimal.ZERO) > 0)
                .map(cuentaMapper::aResumen)
                .toList();
    }




    @Override
    @Transactional
    public boolean validarYRegistrarCargo(Long clienteId, BigDecimal monto, Long pedidoId) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El monto del cargo debe ser mayor que 0.");
        }
        CuentaCorriente cuenta = buscarPorCliente(clienteId);
        if (!cuenta.isActiva()) {
            throw new ReglaNegocioException("La cuenta corriente del cliente no está activa.");
        }
        registrarMovimiento(cuenta, TipoMovimiento.CARGO, monto, pedidoId, "Compra con cuenta corriente");
        return true;
    }




    private CuentaResumenDTO registrarMovimiento(CuentaCorriente cuenta,
                                                 TipoMovimiento tipo,
                                                 BigDecimal monto,
                                                 Long pedidoId,
                                                 String descripcion) {
        BigDecimal nuevoSaldo;
        if (tipo == TipoMovimiento.CARGO) {
            nuevoSaldo = cuenta.getSaldoActual().add(monto);
            if (nuevoSaldo.compareTo(cuenta.getLimiteCredito()) > 0) {
                throw new ReglaNegocioException(
                        "Supera el límite de crédito. Cupo disponible: " + cuenta.getDisponible() + ".");
            }
        } else {
            nuevoSaldo = cuenta.getSaldoActual().subtract(monto);
            if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
                nuevoSaldo = BigDecimal.ZERO;
            }
        }

        cuenta.setSaldoActual(nuevoSaldo);
        CuentaCorriente guardada = cuentaRepository.save(cuenta);

        MovimientoCuenta movimiento = new MovimientoCuenta();
        movimiento.setCuenta(guardada);
        movimiento.setTipo(tipo);
        movimiento.setPedidoId(pedidoId);
        movimiento.setDescripcion(descripcion);
        movimiento.setMonto(monto);
        movimiento.setSaldoPosterior(nuevoSaldo);
        movimientoRepository.save(movimiento);

        log.info("Cuenta {}: {} de {} (saldo posterior {})", guardada.getId(), tipo, monto, nuevoSaldo);
        return cuentaMapper.aResumen(guardada);
    }

    private CuentaCorriente buscarPorCliente(Long clienteId) {
        return cuentaRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El cliente no tiene una cuenta corriente."));
    }
}