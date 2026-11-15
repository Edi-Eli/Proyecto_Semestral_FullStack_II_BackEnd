package com.ferreteria.maestros.usuario.controller;

import com.ferreteria.maestros.usuario.dto.RolDTO;
import com.ferreteria.maestros.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/** Catálogo de roles (solo Administrador). */
@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "Catálogo de roles del sistema.")
public class RolController {

    private final UsuarioService usuarioService;

    public RolController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @PreAuthorize("@autorizacion.esAdmin()")
    @Operation(summary = "Listar roles", description = "Roles disponibles: Administrador, Vendedor y Cliente.")
    @ApiResponse(responseCode = "200", description = "Lista de roles.")
    public CollectionModel<EntityModel<RolDTO>> listar() {
        List<EntityModel<RolDTO>> roles = usuarioService.listarRoles().stream()
                .map(EntityModel::of)
                .toList();
        return CollectionModel.of(roles, linkTo(RolController.class).withSelfRel());
    }
}
