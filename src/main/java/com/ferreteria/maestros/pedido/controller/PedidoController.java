package com.ferreteria.maestros.pedido.controller;

import com.ferreteria.maestros.common.SecurityUtils;
import com.ferreteria.maestros.common.exception.RecursoNoEncontradoException;
import com.ferreteria.maestros.pedido.dto.CambiarEstadoRequest;
import com.ferreteria.maestros.pedido.dto.CheckoutRequest;
import com.ferreteria.maestros.pedido.dto.PedidoDetalleDTO;
import com.ferreteria.maestros.pedido.dto.PedidoResumenDTO;
import com.ferreteria.maestros.pedido.entity.EstadoPedido;
import com.ferreteria.maestros.pedido.service.PedidoService;
import com.ferreteria.maestros.usuario.entity.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Checkout, estados, boleta e historial (doc 02 §4.5).")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @Operation(summary = "Checkout",
            description = "Crea el pedido: valida/descuenta stock y, si el pago es a cuenta corriente, carga la cuenta. Devuelve 201.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido creado."),
            @ApiResponse(responseCode = "404", description = "Algún producto no existe."),
            @ApiResponse(responseCode = "422", description = "Stock insuficiente o sin cupo en la cuenta corriente.")
    })
    public ResponseEntity<EntityModel<PedidoDetalleDTO>> checkout(@Valid @RequestBody CheckoutRequest request) {
        PedidoDetalleDTO creado = pedidoService.checkout(usuarioActual(), request);
        return ResponseEntity
                .created(linkTo(PedidoController.class).slash(creado.id()).toUri())
                .body(conEnlaces(creado));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Listar pedidos", description = "Administrador/Vendedor. Filtro opcional ?estado=.")
    public PagedModel<EntityModel<PedidoResumenDTO>> listar(
            @Parameter(description = "Estado: PENDIENTE, APROBADA, DESPACHADA, ENTREGADA, PAGADA, RECHAZADA")
            @RequestParam(required = false) EstadoPedido estado,
            @ParameterObject @PageableDefault(size = 12) Pageable pageable) {
        Page<PedidoResumenDTO> pagina = pedidoService.listar(estado, pageable);
        List<EntityModel<PedidoResumenDTO>> items = pagina.getContent().stream()
                .map(this::conEnlacesResumen)
                .toList();
        return paginar(items, pagina);
    }

    @GetMapping("/mios")
    @Operation(summary = "Mis pedidos", description = "Cliente autenticado: su historial de compras.")
    public CollectionModel<EntityModel<PedidoResumenDTO>> mios() {
        List<EntityModel<PedidoResumenDTO>> items = pedidoService.mios(usuarioActual().getId()).stream()
                .map(this::conEnlacesResumen)
                .toList();
        return CollectionModel.of(items, linkTo(PedidoController.class).slash("mios").withSelfRel());
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Pedidos de un cliente", description = "Administrador/Vendedor: historial de un usuario.")
    public CollectionModel<EntityModel<PedidoResumenDTO>> porUsuario(@PathVariable Long usuarioId) {
        List<EntityModel<PedidoResumenDTO>> items = pedidoService.porUsuario(usuarioId).stream()
                .map(this::conEnlacesResumen)
                .toList();
        return CollectionModel.of(items,
                linkTo(PedidoController.class).slash("usuario").slash(usuarioId).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un pedido", description = "Administrador/Vendedor o el propio dueño.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado."),
            @ApiResponse(responseCode = "403", description = "Sin permisos sobre este pedido."),
            @ApiResponse(responseCode = "404", description = "El pedido no existe.")
    })
    public EntityModel<PedidoDetalleDTO> detalle(@PathVariable Long id) {
        PedidoDetalleDTO dto = pedidoService.detalle(id);
        verificarAcceso(dto);
        return conEnlaces(dto);
    }

    @GetMapping("/{id}/boleta")
    @Operation(summary = "Boleta", description = "Administrador/Vendedor o el propio dueño.")
    public EntityModel<PedidoDetalleDTO> boleta(@PathVariable Long id) {
        PedidoDetalleDTO dto = pedidoService.boleta(id);
        verificarAcceso(dto);
        return conEnlaces(dto);
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Cambiar estado",
            description = "Administrador/Vendedor. Al pasar a RECHAZADA se repone el stock.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado."),
            @ApiResponse(responseCode = "404", description = "El pedido no existe.")
    })
    public EntityModel<PedidoDetalleDTO> cambiarEstado(@PathVariable Long id,
                                                       @Valid @RequestBody CambiarEstadoRequest request) {
        return conEnlaces(pedidoService.cambiarEstado(id, request.estado()));
    }






    private Usuario usuarioActual() {
        return SecurityUtils.usuarioActual()
                .orElseThrow(() -> new RecursoNoEncontradoException("No hay una sesión activa."));
    }

    private void verificarAcceso(PedidoDetalleDTO dto) {
        Usuario actual = usuarioActual();
        boolean esDuenio = actual.getId().equals(dto.usuarioId());
        if (!esGestion(actual) && !esDuenio) {
            throw new AccessDeniedException("No tiene permisos sobre este pedido.");
        }
    }

    private boolean esGestion(Usuario usuario) {
        String rol = usuario.getRol().getNombre();
        return "Administrador".equalsIgnoreCase(rol) || "Vendedor".equalsIgnoreCase(rol);
    }

    private EntityModel<PedidoDetalleDTO> conEnlaces(PedidoDetalleDTO dto) {
        return EntityModel.of(dto, linkTo(PedidoController.class).slash(dto.id()).withSelfRel());
    }

    private EntityModel<PedidoResumenDTO> conEnlacesResumen(PedidoResumenDTO dto) {
        return EntityModel.of(dto, linkTo(PedidoController.class).slash(dto.id()).withSelfRel());
    }

    private <T> PagedModel<EntityModel<T>> paginar(List<EntityModel<T>> items, Page<?> pagina) {
        PagedModel.PageMetadata metadatos = new PagedModel.PageMetadata(
                pagina.getSize(), pagina.getNumber(), pagina.getTotalElements(), pagina.getTotalPages());
        return PagedModel.of(items, metadatos, linkTo(PedidoController.class).withSelfRel());
    }
}