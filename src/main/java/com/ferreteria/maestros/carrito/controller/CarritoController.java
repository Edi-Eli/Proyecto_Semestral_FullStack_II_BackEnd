package com.ferreteria.maestros.carrito.controller;

import com.ferreteria.maestros.carrito.dto.ActualizarCantidadRequest;
import com.ferreteria.maestros.carrito.dto.AgregarItemRequest;
import com.ferreteria.maestros.carrito.dto.CarritoDTO;
import com.ferreteria.maestros.carrito.service.CarritoService;
import com.ferreteria.maestros.common.SecurityUtils;
import com.ferreteria.maestros.common.exception.RecursoNoEncontradoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/carrito")
@Tag(name = "Carrito", description = "Carrito persistente del usuario autenticado.")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    @Operation(summary = "Obtener mi carrito", description = "Carrito del usuario autenticado (puede venir vacío).")
    @ApiResponse(responseCode = "200", description = "Carrito del usuario.")
    public EntityModel<CarritoDTO> obtener() {
        return conEnlaces(carritoService.obtener(usuarioId()));
    }

    @PostMapping("/items")
    @Operation(summary = "Agregar producto al carrito",
            description = "Valida stock y guarda el snapshot de nombre/precio/imagen. Cuerpo: { productoId, cantidad }.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito actualizado."),
            @ApiResponse(responseCode = "404", description = "El producto no existe."),
            @ApiResponse(responseCode = "422", description = "Stock insuficiente.")
    })
    public EntityModel<CarritoDTO> agregar(@Valid @RequestBody AgregarItemRequest request) {
        return conEnlaces(carritoService.agregar(usuarioId(), request.productoId(), request.cantidad()));
    }

    @PutMapping("/items/{productoId}")
    @Operation(summary = "Actualizar cantidad de un item",
            description = "Cuerpo: { cantidad }. Cantidad 0 elimina el item. Valida stock.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito actualizado."),
            @ApiResponse(responseCode = "422", description = "Stock insuficiente.")
    })
    public EntityModel<CarritoDTO> actualizarCantidad(@PathVariable Long productoId,
                                                      @Valid @RequestBody ActualizarCantidadRequest request) {
        return conEnlaces(carritoService.actualizarCantidad(usuarioId(), productoId, request.cantidad()));
    }

    @DeleteMapping("/items/{productoId}")
    @Operation(summary = "Quitar un item del carrito", description = "Elimina el producto del carrito.")
    @ApiResponse(responseCode = "200", description = "Carrito actualizado.")
    public EntityModel<CarritoDTO> eliminar(@PathVariable Long productoId) {
        return conEnlaces(carritoService.eliminar(usuarioId(), productoId));
    }

    @DeleteMapping
    @Operation(summary = "Vaciar el carrito", description = "Elimina todos los items del carrito.")
    @ApiResponse(responseCode = "200", description = "Carrito vacío.")
    public EntityModel<CarritoDTO> vaciar() {
        return conEnlaces(carritoService.vaciar(usuarioId()));
    }

    private Long usuarioId() {
        return SecurityUtils.usuarioActual()
                .orElseThrow(() -> new RecursoNoEncontradoException("No hay una sesión activa."))
                .getId();
    }

    private EntityModel<CarritoDTO> conEnlaces(CarritoDTO dto) {
        return EntityModel.of(dto, linkTo(CarritoController.class).withSelfRel());
    }
}
