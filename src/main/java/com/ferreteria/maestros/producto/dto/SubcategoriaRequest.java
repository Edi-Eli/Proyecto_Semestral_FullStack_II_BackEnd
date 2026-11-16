package com.ferreteria.maestros.producto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubcategoriaRequest(

        @NotNull(message = "Debe indicar la categoría.")
        Long categoriaId,

        @NotBlank(message = "El nombre de la subcategoría es obligatorio.")
        @Size(max = 80, message = "El nombre no puede superar los 80 caracteres.")
        String nombre
) {
}
