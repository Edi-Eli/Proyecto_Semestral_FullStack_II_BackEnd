package com.ferreteria.maestros.producto.controller;

import com.ferreteria.maestros.producto.dto.SubcategoriaDTO;
import com.ferreteria.maestros.producto.dto.SubcategoriaRequest;
import com.ferreteria.maestros.producto.service.SubcategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/subcategorias")
@Tag(name = "Subcategorías", description = "Subcategorías del catálogo (lectura pública, administración ADM).")
public class SubcategoriaController {

    private final SubcategoriaService subcategoriaService;

    public SubcategoriaController(SubcategoriaService subcategoriaService) {
        this.subcategoriaService = subcategoriaService;
    }

    @GetMapping
    @Operation(summary = "Listar subcategorías",
            description = "Público. Filtro opcional ?categoriaId=. Respuesta HATEOAS.")
    @ApiResponse(responseCode = "200", description = "Lista de subcategorías.")
    public CollectionModel<EntityModel<SubcategoriaDTO>> listar(
            @Parameter(description = "Filtrar por categoría") @RequestParam(required = false) Long categoriaId) {
        List<EntityModel<SubcategoriaDTO>> items = subcategoriaService.listar(categoriaId).stream()
                .map(this::conEnlaces)
                .toList();
        return CollectionModel.of(items, linkTo(SubcategoriaController.class).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una subcategoría", description = "Público.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subcategoría encontrada."),
            @ApiResponse(responseCode = "404", description = "La subcategoría no existe.")
    })
    public EntityModel<SubcategoriaDTO> obtener(@PathVariable Long id) {
        return conEnlaces(subcategoriaService.obtener(id));
    }

    @PostMapping
    @PreAuthorize("@autorizacion.esAdmin()")
    @Operation(summary = "Crear subcategoría", description = "Solo Administrador.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Subcategoría creada."),
            @ApiResponse(responseCode = "404", description = "La categoría no existe."),
            @ApiResponse(responseCode = "409", description = "La subcategoría ya existe en esa categoría.")
    })
    public ResponseEntity<EntityModel<SubcategoriaDTO>> crear(@Valid @RequestBody SubcategoriaRequest request) {
        SubcategoriaDTO creada = subcategoriaService.crear(request);
        return ResponseEntity
                .created(linkTo(methodOn(SubcategoriaController.class).obtener(creada.id())).toUri())
                .body(conEnlaces(creada));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@autorizacion.esAdmin()")
    @Operation(summary = "Editar subcategoría", description = "Solo Administrador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subcategoría actualizada."),
            @ApiResponse(responseCode = "404", description = "La subcategoría o categoría no existe."),
            @ApiResponse(responseCode = "409", description = "La subcategoría ya existe en esa categoría.")
    })
    public EntityModel<SubcategoriaDTO> actualizar(@PathVariable Long id,
                                                   @Valid @RequestBody SubcategoriaRequest request) {
        return conEnlaces(subcategoriaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@autorizacion.esAdmin()")
    @Operation(summary = "Eliminar subcategoría",
            description = "Solo Administrador. Falla si la subcategoría tiene productos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Subcategoría eliminada."),
            @ApiResponse(responseCode = "404", description = "La subcategoría no existe."),
            @ApiResponse(responseCode = "422", description = "La subcategoría tiene productos asociados.")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        subcategoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<SubcategoriaDTO> conEnlaces(SubcategoriaDTO dto) {
        WebMvcLinkBuilder self = linkTo(methodOn(SubcategoriaController.class).obtener(dto.id()));
        return EntityModel.of(dto, self.withSelfRel());
    }
}
