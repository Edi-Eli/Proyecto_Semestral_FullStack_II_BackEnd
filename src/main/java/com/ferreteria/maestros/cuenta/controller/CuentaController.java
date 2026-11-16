package com.ferreteria.maestros.cuenta.controller;

import com.ferreteria.maestros.common.SecurityUtils;
import com.ferreteria.maestros.common.exception.RecursoNoEncontradoException;
import com.ferreteria.maestros.cuenta.dto.AbrirCuentaRequest;
import com.ferreteria.maestros.cuenta.dto.AbonoRequest;
import com.ferreteria.maestros.cuenta.dto.CuentaResumenDTO;
import com.ferreteria.maestros.cuenta.dto.LimiteRequest;
import com.ferreteria.maestros.cuenta.dto.MovimientoDTO;
import com.ferreteria.maestros.cuenta.service.CuentaService;
import com.ferreteria.maestros.usuario.entity.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/cuentas")
@Tag(name = "Cuentas corrientes", description = "Saldo, movimientos, abonos y cobros (doc 02 §4.6).")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping("/mia")
    @Operation(summary = "Mi cuenta", description = "Cliente autenticado: su saldo y cupo disponible.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada."),
            @ApiResponse(responseCode = "404", description = "El usuario no tiene cuenta corriente.")
    })
    public EntityModel<CuentaResumenDTO> miCuenta() {
        return conEnlaces(cuentaService.miCuenta(usuarioActual().getId()));
    }

    @GetMapping("/resumen-cobros")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Resumen de cobros", description = "Administrador/Vendedor: saldos pendientes de todos los clientes.")
    public CollectionModel<EntityModel<CuentaResumenDTO>> resumenCobros() {
        List<EntityModel<CuentaResumenDTO>> items = cuentaService.resumenCobros().stream()
                .map(this::conEnlaces)
                .toList();
        return CollectionModel.of(items,
                linkTo(CuentaController.class).slash("resumen-cobros").withSelfRel());
    }

    @GetMapping("/{clienteId}")
    @Operation(summary = "Cuenta de un cliente", description = "Administrador/Vendedor o el propio dueño.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada."),
            @ApiResponse(responseCode = "403", description = "Sin permisos sobre esta cuenta."),
            @ApiResponse(responseCode = "404", description = "El cliente no tiene cuenta corriente.")
    })
    public EntityModel<CuentaResumenDTO> obtener(@PathVariable Long clienteId) {
        verificarAcceso(clienteId);
        return conEnlaces(cuentaService.obtenerCuenta(clienteId));
    }

    @GetMapping("/{clienteId}/movimientos")
    @Operation(summary = "Movimientos de una cuenta",
            description = "Administrador/Vendedor o el propio dueño. Filtros opcionales ?desde=yyyy-MM-dd&hasta=yyyy-MM-dd.")
    public CollectionModel<EntityModel<MovimientoDTO>> movimientos(
            @PathVariable Long clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        verificarAcceso(clienteId);
        List<EntityModel<MovimientoDTO>> items = cuentaService.listarMovimientos(clienteId, desde, hasta).stream()
                .map(movimiento -> EntityModel.of(movimiento))
                .toList();
        return CollectionModel.of(items,
                linkTo(CuentaController.class).slash(clienteId).slash("movimientos").withSelfRel());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Abrir cuenta corriente", description = "Solo Administrador. Devuelve 201.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada."),
            @ApiResponse(responseCode = "404", description = "El cliente no existe."),
            @ApiResponse(responseCode = "409", description = "El cliente ya tiene cuenta corriente.")
    })
    public ResponseEntity<EntityModel<CuentaResumenDTO>> abrir(@Valid @RequestBody AbrirCuentaRequest request) {
        CuentaResumenDTO creada = cuentaService.abrirCuenta(request);
        return ResponseEntity
                .created(linkTo(CuentaController.class).slash(creada.clienteId()).toUri())
                .body(conEnlaces(creada));
    }

    @PatchMapping("/{clienteId}/limite")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Ajustar límite de crédito", description = "Solo Administrador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Límite actualizado."),
            @ApiResponse(responseCode = "404", description = "El cliente no tiene cuenta corriente."),
            @ApiResponse(responseCode = "422", description = "El límite es menor que el saldo usado.")
    })
    public EntityModel<CuentaResumenDTO> ajustarLimite(@PathVariable Long clienteId,
                                                       @Valid @RequestBody LimiteRequest request) {
        return conEnlaces(cuentaService.ajustarLimite(clienteId, request));
    }

    @PostMapping("/{clienteId}/abonos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Registrar abono", description = "Administrador/Vendedor: pago a fin de mes.")
    public EntityModel<CuentaResumenDTO> abonar(@PathVariable Long clienteId,
                                                @Valid @RequestBody AbonoRequest request) {
        return conEnlaces(cuentaService.registrarAbono(clienteId, request));
    }







    private Usuario usuarioActual() {
        return SecurityUtils.usuarioActual()
                .orElseThrow(() -> new RecursoNoEncontradoException("No hay una sesión activa."));
    }

    private void verificarAcceso(Long clienteId) {
        Usuario actual = usuarioActual();
        boolean esDuenio = clienteId.equals(actual.getId());
        if (!esGestion(actual) && !esDuenio) {
            throw new AccessDeniedException("No tiene permisos sobre esta cuenta.");
        }
    }

    private boolean esGestion(Usuario usuario) {
        String rol = usuario.getRol().getNombre();
        return "Administrador".equalsIgnoreCase(rol) || "Vendedor".equalsIgnoreCase(rol);
    }

    private EntityModel<CuentaResumenDTO> conEnlaces(CuentaResumenDTO dto) {
        return EntityModel.of(dto, linkTo(CuentaController.class).slash(dto.clienteId()).withSelfRel());
    }
}