package com.ferreteria.maestros.usuario.controller;

import com.ferreteria.maestros.common.SecurityUtils;
import com.ferreteria.maestros.pedido.dto.PedidoResumenDTO;
import com.ferreteria.maestros.usuario.dto.CambiarEstadoRequest;
import com.ferreteria.maestros.usuario.dto.UsuarioActualizarRequest;
import com.ferreteria.maestros.usuario.dto.UsuarioCrearRequest;
import com.ferreteria.maestros.usuario.dto.UsuarioDTO;
import com.ferreteria.maestros.usuario.service.UsuarioService;
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
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Endpoints de usuarios (panel de administración) con HATEOAS y autorización por rol.
 *
 * <p>Las rutas son por <b>RUN</b> (como espera el front). El acceso se controla con
 * {@code @PreAuthorize}: los listados y la administración requieren Administrador; el detalle,
 * la actualización y el historial permiten también al <b>dueño</b> de la cuenta.</p>
 */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios (panel de administración).")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @PreAuthorize("@autorizacion.esAdmin()")
    @Operation(summary = "Listar usuarios",
            description = "Paginado con filtros opcionales (?rol=&activo=&q=). Respuesta HATEOAS.")
    @ApiResponse(responseCode = "200", description = "Página de usuarios.")
    public PagedModel<EntityModel<UsuarioDTO>> listar(
            @Parameter(description = "Nombre del rol (Administrador, Vendedor, Cliente)")
            @RequestParam(required = false) String rol,
            @Parameter(description = "Filtrar por estado activo/inactivo")
            @RequestParam(required = false) Boolean activo,
            @Parameter(description = "Texto libre: nombre, apellidos, correo o RUN")
            @RequestParam(required = false) String q,
            @ParameterObject @PageableDefault(size = 20, sort = "apellidos") Pageable pageable) {

        Page<UsuarioDTO> pagina = usuarioService.listar(rol, activo, q, pageable);
        List<EntityModel<UsuarioDTO>> items = pagina.getContent().stream()
                .map(this::conEnlaces)
                .toList();

        PagedModel.PageMetadata metadatos = new PagedModel.PageMetadata(
                pagina.getSize(), pagina.getNumber(), pagina.getTotalElements(), pagina.getTotalPages());

        return PagedModel.of(items, metadatos, linkTo(UsuarioController.class).withSelfRel());
    }

    @GetMapping("/{run}")
    @PreAuthorize("@autorizacion.esAdminODuenio(#run)")
    @Operation(summary = "Detalle de un usuario",
            description = "Disponible para Administrador o para el propio usuario. No expone la contraseña.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado."),
            @ApiResponse(responseCode = "403", description = "Sin permisos (no es administrador ni el dueño)."),
            @ApiResponse(responseCode = "404", description = "El RUN no existe.")
    })
    public EntityModel<UsuarioDTO> obtener(@PathVariable String run) {
        return conEnlaces(usuarioService.obtener(run));
    }

    @GetMapping("/{run}/historial")
    @PreAuthorize("@autorizacion.esAdminODuenio(#run)")
    @Operation(summary = "Historial de compras de un usuario",
            description = "Pedidos del cliente (contrato con el módulo pedido). Puede venir vacío hasta la Fase 6.")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos (puede estar vacía).")
    public CollectionModel<PedidoResumenDTO> historial(@PathVariable String run) {
        List<PedidoResumenDTO> pedidos = usuarioService.historial(run);
        return CollectionModel.of(pedidos,
                linkTo(methodOn(UsuarioController.class).historial(run)).withSelfRel());
    }

    @PostMapping
    @PreAuthorize("@autorizacion.esAdmin()")
    @Operation(summary = "Crear usuario", description = "Solo Administrador. Devuelve 201 con el usuario creado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado."),
            @ApiResponse(responseCode = "409", description = "Correo o RUN ya registrados."),
            @ApiResponse(responseCode = "422", description = "RUN inválido o rol inexistente.")
    })
    public ResponseEntity<EntityModel<UsuarioDTO>> crear(@Valid @RequestBody UsuarioCrearRequest request) {
        UsuarioDTO creado = usuarioService.crear(request);
        EntityModel<UsuarioDTO> modelo = conEnlaces(creado);
        return ResponseEntity
                .created(linkTo(methodOn(UsuarioController.class).obtener(creado.run())).toUri())
                .body(modelo);
    }

    @PutMapping("/{run}")
    @PreAuthorize("@autorizacion.esAdminODuenio(#run)")
    @Operation(summary = "Actualizar usuario",
            description = "Administrador o el propio usuario. Solo el Administrador puede cambiar rol, estado o cuenta corriente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado."),
            @ApiResponse(responseCode = "403", description = "Sin permisos."),
            @ApiResponse(responseCode = "404", description = "El RUN no existe."),
            @ApiResponse(responseCode = "409", description = "El correo pertenece a otro usuario.")
    })
    public EntityModel<UsuarioDTO> actualizar(@PathVariable String run,
                                             @Valid @RequestBody UsuarioActualizarRequest request) {
        UsuarioDTO actualizado = usuarioService.actualizar(run, request, SecurityUtils.esAdmin());
        return conEnlaces(actualizado);
    }

    @PatchMapping("/{run}/estado")
    @PreAuthorize("@autorizacion.esAdmin()")
    @Operation(summary = "Activar / desactivar usuario",
            description = "Solo Administrador. Cuerpo: { \"activo\": true|false }.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado."),
            @ApiResponse(responseCode = "403", description = "Sin permisos."),
            @ApiResponse(responseCode = "404", description = "El RUN no existe.")
    })
    public EntityModel<UsuarioDTO> cambiarEstado(@PathVariable String run,
                                                @Valid @RequestBody CambiarEstadoRequest request) {
        return conEnlaces(usuarioService.cambiarEstado(run, request.activo()));
    }

    @DeleteMapping("/{run}")
    @PreAuthorize("@autorizacion.esAdmin()")
    @Operation(summary = "Eliminar usuario",
            description = "Solo Administrador. Se implementa como baja lógica (activo=false). Responde 204.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario dado de baja."),
            @ApiResponse(responseCode = "404", description = "El RUN no existe.")
    })
    public ResponseEntity<Void> eliminar(@PathVariable String run) {
        usuarioService.eliminar(run);
        return ResponseEntity.noContent().build();
    }

    /** Añade los enlaces HATEOAS a un usuario. */
    private EntityModel<UsuarioDTO> conEnlaces(UsuarioDTO dto) {
        WebMvcLinkBuilder self = linkTo(methodOn(UsuarioController.class).obtener(dto.run()));
        return EntityModel.of(dto,
                self.withSelfRel(),
                linkTo(methodOn(UsuarioController.class).historial(dto.run())).withRel("historial"));
    }
}
