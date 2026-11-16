package com.ferreteria.maestros.producto.controller;

import com.ferreteria.maestros.common.SecurityUtils;
import com.ferreteria.maestros.producto.dto.ActualizarStockRequest;
import com.ferreteria.maestros.producto.dto.ProductoAdminDTO;
import com.ferreteria.maestros.producto.dto.ProductoDTO;
import com.ferreteria.maestros.producto.dto.ProductoRequest;
import com.ferreteria.maestros.producto.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Catálogo de productos (lectura pública, gestión ADM/VEN).")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar productos",
            description = "Público. Filtros ?q=&categoriaId=&subcategoriaId=&enOferta=&orden=(precio-asc|precio-desc|nombre). "
                    + "Para Administrador/Vendedor incluye precioCompra.")
    @ApiResponse(responseCode = "200", description = "Página de productos (HATEOAS).")
    public PagedModel<?> listar(
            @Parameter(description = "Texto libre: nombre, descripción, marca o código")
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long subcategoriaId,
            @RequestParam(required = false) Boolean enOferta,
            @Parameter(description = "Orden: precio-asc, precio-desc o nombre")
            @RequestParam(required = false) String orden,
            @ParameterObject @PageableDefault(size = 12) Pageable pageable) {

        Pageable paginado = conOrden(pageable, orden);
        if (esGestion()) {
            Page<ProductoAdminDTO> pagina = productoService.listarAdmin(
                    q, categoriaId, subcategoriaId, enOferta, paginado);
            List<EntityModel<ProductoAdminDTO>> items = pagina.getContent().stream()
                    .map(this::conEnlacesAdmin)
                    .toList();
            return paginar(items, pagina);
        }
        Page<ProductoDTO> pagina = productoService.listar(q, categoriaId, subcategoriaId, enOferta, paginado);
        List<EntityModel<ProductoDTO>> items = pagina.getContent().stream()
                .map(this::conEnlaces)
                .toList();
        return paginar(items, pagina);
    }

    @GetMapping("/ofertas")
    @Operation(summary = "Listar ofertas", description = "Público. Productos en oferta con precioOferta > 0.")
    @ApiResponse(responseCode = "200", description = "Página de ofertas (HATEOAS).")
    public PagedModel<EntityModel<ProductoDTO>> ofertas(
            @ParameterObject @PageableDefault(size = 12) Pageable pageable) {
        Page<ProductoDTO> pagina = productoService.listarOfertas(pageable);
        List<EntityModel<ProductoDTO>> items = pagina.getContent().stream()
                .map(this::conEnlaces)
                .toList();
        return paginar(items, pagina);
    }

    @GetMapping("/criticos")
    @Operation(summary = "Listar productos críticos",
            description = "Administrador/Vendedor. Productos con stock <= stock mínimo.")
    @ApiResponse(responseCode = "200", description = "Lista de productos críticos.")
    public CollectionModel<EntityModel<ProductoDTO>> criticos() {
        List<EntityModel<ProductoDTO>> items = productoService.listarCriticos().stream()
                .map(this::conEnlaces)
                .toList();
        return CollectionModel.of(items, linkTo(ProductoController.class).slash("criticos").withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un producto",
            description = "Público. Para Administrador/Vendedor incluye precioCompra.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado."),
            @ApiResponse(responseCode = "404", description = "El producto no existe.")
    })
    public EntityModel<?> obtener(@PathVariable Long id) {
        return esGestion()
                ? conEnlacesAdmin(productoService.obtenerAdmin(id))
                : conEnlaces(productoService.obtener(id));
    }

    @PostMapping
    @Operation(summary = "Crear producto", description = "Administrador/Vendedor. Devuelve 201.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado."),
            @ApiResponse(responseCode = "404", description = "La subcategoría no existe."),
            @ApiResponse(responseCode = "409", description = "El código ya existe.")
    })
    public ResponseEntity<EntityModel<ProductoAdminDTO>> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoAdminDTO creado = productoService.crear(request);
        return ResponseEntity
                .created(linkTo(ProductoController.class).slash(creado.id()).toUri())
                .body(conEnlacesAdmin(creado));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar producto", description = "Administrador/Vendedor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado."),
            @ApiResponse(responseCode = "404", description = "El producto o la subcategoría no existe."),
            @ApiResponse(responseCode = "409", description = "El código ya existe.")
    })
    public EntityModel<ProductoAdminDTO> actualizar(@PathVariable Long id,
                                                    @Valid @RequestBody ProductoRequest request) {
        return conEnlacesAdmin(productoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Actualizar stock",
            description = "Administrador/Vendedor. Cuerpo: { \"cantidad\": N, \"accion\": \"AGREGAR\"|\"RETIRAR\" }.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock actualizado."),
            @ApiResponse(responseCode = "404", description = "El producto no existe."),
            @ApiResponse(responseCode = "422", description = "Stock insuficiente para retirar.")
    })
    public EntityModel<ProductoAdminDTO> actualizarStock(@PathVariable Long id,
                                                         @Valid @RequestBody ActualizarStockRequest request) {
        return conEnlacesAdmin(productoService.actualizarStock(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto",
            description = "Solo Administrador. Se implementa como baja lógica (activo=false). Responde 204.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto dado de baja."),
            @ApiResponse(responseCode = "404", description = "El producto no existe.")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private boolean esGestion() {
        return SecurityUtils.usuarioActual()
                .map(usuario -> {
                    String rol = usuario.getRol().getNombre();
                    return "Administrador".equalsIgnoreCase(rol) || "Vendedor".equalsIgnoreCase(rol);
                })
                .orElse(false);
    }

    private Pageable conOrden(Pageable pageable, String orden) {
        Sort sort = switch (orden == null ? "" : orden) {
            case "precio-asc" -> Sort.by("precioVenta").ascending();
            case "precio-desc" -> Sort.by("precioVenta").descending();
            case "nombre" -> Sort.by("nombre").ascending();
            default -> pageable.getSort().isSorted() ? pageable.getSort() : Sort.by("nombre").ascending();
        };
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private <T> PagedModel<EntityModel<T>> paginar(List<EntityModel<T>> items, Page<?> pagina) {
        PagedModel.PageMetadata metadatos = new PagedModel.PageMetadata(
                pagina.getSize(), pagina.getNumber(), pagina.getTotalElements(), pagina.getTotalPages());
        return PagedModel.of(items, metadatos, linkTo(ProductoController.class).withSelfRel());
    }

    private EntityModel<ProductoDTO> conEnlaces(ProductoDTO dto) {
        return EntityModel.of(dto, linkTo(ProductoController.class).slash(dto.id()).withSelfRel());
    }

    private EntityModel<ProductoAdminDTO> conEnlacesAdmin(ProductoAdminDTO dto) {
        return EntityModel.of(dto, linkTo(ProductoController.class).slash(dto.id()).withSelfRel());
    }
}
