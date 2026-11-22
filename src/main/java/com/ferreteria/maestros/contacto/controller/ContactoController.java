package com.ferreteria.maestros.contacto.controller;

import com.ferreteria.maestros.contacto.dto.CambiarEstadoMensajeRequest;
import com.ferreteria.maestros.contacto.dto.CrearMensajeRequest;
import com.ferreteria.maestros.contacto.dto.MensajeContactoDTO;
import com.ferreteria.maestros.contacto.entity.EstadoMensaje;
import com.ferreteria.maestros.contacto.service.ContactoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
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
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Mensajes de contacto (doc 02 §4.7). El <b>POST es público</b> (formulario del sitio);
 * el resto es para Administrador o Vendedor.
 */
@RestController
@RequestMapping("/api/contactos")
@Tag(name = "Contacto", description = "Mensajes enviados desde el formulario de contacto.")
public class ContactoController {

    private final ContactoService contactoService;

    public ContactoController(ContactoService contactoService) {
        this.contactoService = contactoService;
    }

    @PostMapping
    @Operation(summary = "Enviar mensaje", description = "Público. Registra el mensaje y devuelve 201.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensaje registrado."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos.")
    })
    public ResponseEntity<EntityModel<MensajeContactoDTO>> crear(@Valid @RequestBody CrearMensajeRequest request) {
        MensajeContactoDTO creado = contactoService.crear(request);
        EntityModel<MensajeContactoDTO> modelo = conEnlaces(creado);
        return ResponseEntity
                .created(linkTo(methodOn(ContactoController.class).obtener(creado.id())).toUri())
                .body(modelo);
    }

    @GetMapping
    @PreAuthorize("@autorizacion.esAdminOVendedor()")
    @Operation(summary = "Listar mensajes",
            description = "Administrador/Vendedor. Filtro opcional ?estado=NUEVO|LEIDO|RESUELTO (más recientes primero).")
    @ApiResponse(responseCode = "200", description = "Lista de mensajes.")
    public CollectionModel<EntityModel<MensajeContactoDTO>> listar(
            @Parameter(description = "Estado del mensaje: NUEVO, LEIDO o RESUELTO")
            @RequestParam(required = false) EstadoMensaje estado) {
        List<EntityModel<MensajeContactoDTO>> items = contactoService.listar(estado).stream()
                .map(this::conEnlaces)
                .toList();
        return CollectionModel.of(items, linkTo(ContactoController.class).withSelfRel());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@autorizacion.esAdminOVendedor()")
    @Operation(summary = "Detalle de un mensaje", description = "Administrador/Vendedor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mensaje encontrado."),
            @ApiResponse(responseCode = "404", description = "El mensaje no existe.")
    })
    public EntityModel<MensajeContactoDTO> obtener(@PathVariable Long id) {
        return conEnlaces(contactoService.obtener(id));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("@autorizacion.esAdminOVendedor()")
    @Operation(summary = "Cambiar estado del mensaje",
            description = "Administrador/Vendedor. Cuerpo: { \"estado\": \"LEIDO\" | \"RESUELTO\" }.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado."),
            @ApiResponse(responseCode = "404", description = "El mensaje no existe.")
    })
    public EntityModel<MensajeContactoDTO> cambiarEstado(@PathVariable Long id,
                                                        @Valid @RequestBody CambiarEstadoMensajeRequest request) {
        return conEnlaces(contactoService.cambiarEstado(id, request.estado()));
    }

    private EntityModel<MensajeContactoDTO> conEnlaces(MensajeContactoDTO dto) {
        return EntityModel.of(dto, linkTo(methodOn(ContactoController.class).obtener(dto.id())).withSelfRel());
    }
}
