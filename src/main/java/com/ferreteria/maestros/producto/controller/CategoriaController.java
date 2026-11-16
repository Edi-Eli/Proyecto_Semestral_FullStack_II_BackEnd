package com.ferreteria.maestros.producto.controller;

import com.ferreteria.maestros.producto.dto.CategoriaDTO;
import com.ferreteria.maestros.producto.dto.CategoriaRequest;
import com.ferreteria.maestros.producto.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Categorías del catálogo (lectura pública, administración ADM).")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Público. Respuesta HATEOAS.")
    @ApiResponse(responseCode = "200", description = "Lista de categorías.")
    public CollectionModel<EntityModel<CategoriaDTO>> listar() {
        List<EntityModel<CategoriaDTO>> items = categoriaService.listar().stream()
                .map(this::conEnlaces)
                .toList();
        return CollectionModel.of(items, linkTo(CategoriaController.class).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una categoría", description = "Público.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría encontrada."),
            @ApiResponse(responseCode = "404", description = "La categoría no existe.")
    })
    public EntityModel<CategoriaDTO> obtener(@PathVariable Long id) {
        return conEnlaces(categoriaService.obtener(id));
    }

    @PostMapping
    @PreAuthorize("@autorizacion.esAdmin()")
    @Operation(summary = "Crear categoría", description = "Solo Administrador.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada."),
            @ApiResponse(responseCode = "409", description = "El nombre ya existe.")
    })
    public ResponseEntity<EntityModel<CategoriaDTO>> crear(@Valid @RequestBody CategoriaRequest request) {
        CategoriaDTO creada = categoriaService.crear(request);
        return ResponseEntity
                .created(linkTo(methodOn(CategoriaController.class).obtener(creada.id())).toUri())
                .body(conEnlaces(creada));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@autorizacion.esAdmin()")
    @Operation(summary = "Editar categoría", description = "Solo Administrador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada."),
            @ApiResponse(responseCode = "404", description = "La categoría no existe."),
            @ApiResponse(responseCode = "409", description = "El nombre ya existe.")
    })
    public EntityModel<CategoriaDTO> actualizar(@PathVariable Long id,
                                                @Valid @RequestBody CategoriaRequest request) {
        return conEnlaces(categoriaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@autorizacion.esAdmin()")
    @Operation(summary = "Eliminar categoría",
            description = "Solo Administrador. Falla si la categoría tiene subcategorías.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoría eliminada."),
            @ApiResponse(responseCode = "404", description = "La categoría no existe."),
            @ApiResponse(responseCode = "422", description = "La categoría tiene subcategorías asociadas.")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<CategoriaDTO> conEnlaces(CategoriaDTO dto) {
        WebMvcLinkBuilder self = linkTo(methodOn(CategoriaController.class).obtener(dto.id()));
        return EntityModel.of(dto, self.withSelfRel());
    }
}
