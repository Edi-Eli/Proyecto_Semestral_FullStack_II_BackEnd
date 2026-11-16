package com.ferreteria.maestros.cuenta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ferreteria.maestros.common.exception.RecursoDuplicadoException;
import com.ferreteria.maestros.common.exception.RecursoNoEncontradoException;
import com.ferreteria.maestros.common.exception.ReglaNegocioException;
import com.ferreteria.maestros.cuenta.dto.AbrirCuentaRequest;
import com.ferreteria.maestros.cuenta.dto.AbonoRequest;
import com.ferreteria.maestros.cuenta.entity.CuentaCorriente;
import com.ferreteria.maestros.cuenta.entity.MovimientoCuenta;
import com.ferreteria.maestros.cuenta.entity.TipoMovimiento;
import com.ferreteria.maestros.cuenta.mapper.CuentaMapper;
import com.ferreteria.maestros.cuenta.repository.CuentaCorrienteRepository;
import com.ferreteria.maestros.cuenta.repository.MovimientoCuentaRepository;
import com.ferreteria.maestros.usuario.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    private static final Long CLIENTE = 7L;

    @Mock
    private CuentaCorrienteRepository cuentaRepository;

    @Mock
    private MovimientoCuentaRepository movimientoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CuentaMapper cuentaMapper;

    @InjectMocks
    private CuentaService cuentaService;

    private CuentaCorriente cuenta(String limite, String saldo, boolean activa) {
        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setId(1L);
        cuenta.setLimiteCredito(new BigDecimal(limite));
        cuenta.setSaldoActual(new BigDecimal(saldo));
        cuenta.setActiva(activa);
        return cuenta;
    }

    @Test
    void cargoRegistraSaldoYMovimiento() {
        CuentaCorriente cuenta = cuenta("500000", "0", true);
        when(cuentaRepository.findByClienteId(CLIENTE)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any(CuentaCorriente.class))).thenAnswer(inv -> inv.getArgument(0));

        cuentaService.validarYRegistrarCargo(CLIENTE, new BigDecimal("100000"), 5L);

        assertEquals(new BigDecimal("100000"), cuenta.getSaldoActual());

        ArgumentCaptor<MovimientoCuenta> captor = ArgumentCaptor.forClass(MovimientoCuenta.class);
        verify(movimientoRepository).save(captor.capture());
        assertEquals(TipoMovimiento.CARGO, captor.getValue().getTipo());
        assertEquals(new BigDecimal("100000"), captor.getValue().getSaldoPosterior());
        assertEquals(5L, captor.getValue().getPedidoId());
    }

    @Test
    void cargoQueSuperaElLimiteLanzaReglaDeNegocio() {
        CuentaCorriente cuenta = cuenta("500000", "480000", true);
        when(cuentaRepository.findByClienteId(CLIENTE)).thenReturn(Optional.of(cuenta));

        assertThrows(ReglaNegocioException.class,
                () -> cuentaService.validarYRegistrarCargo(CLIENTE, new BigDecimal("50000"), 1L));

        verify(cuentaRepository, never()).save(any());
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void cargoEnCuentaInactivaLanzaReglaDeNegocio() {
        CuentaCorriente cuenta = cuenta("500000", "0", false);
        when(cuentaRepository.findByClienteId(CLIENTE)).thenReturn(Optional.of(cuenta));

        assertThrows(ReglaNegocioException.class,
                () -> cuentaService.validarYRegistrarCargo(CLIENTE, new BigDecimal("1000"), 1L));
    }

    @Test
    void abonoReduceElSaldo() {
        CuentaCorriente cuenta = cuenta("500000", "120000", true);
        when(cuentaRepository.findByClienteId(CLIENTE)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any(CuentaCorriente.class))).thenAnswer(inv -> inv.getArgument(0));

        cuentaService.registrarAbono(CLIENTE, new AbonoRequest(new BigDecimal("20000")));

        assertEquals(new BigDecimal("100000"), cuenta.getSaldoActual());
    }

    @Test
    void abonoMayorQueElSaldoDejaCero() {
        CuentaCorriente cuenta = cuenta("500000", "10000", true);
        when(cuentaRepository.findByClienteId(CLIENTE)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any(CuentaCorriente.class))).thenAnswer(inv -> inv.getArgument(0));

        cuentaService.registrarAbono(CLIENTE, new AbonoRequest(new BigDecimal("50000")));

        assertEquals(BigDecimal.ZERO, cuenta.getSaldoActual());
    }

    @Test
    void cuentaInexistenteLanzaRecursoNoEncontrado() {
        when(cuentaRepository.findByClienteId(CLIENTE)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> cuentaService.obtenerCuenta(CLIENTE));
    }

    @Test
    void abrirCuentaDuplicadaLanzaRecursoDuplicado() {
        when(cuentaRepository.existsByClienteId(CLIENTE)).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class,
                () -> cuentaService.abrirCuenta(new AbrirCuentaRequest(CLIENTE, new BigDecimal("100000"))));
    }
}