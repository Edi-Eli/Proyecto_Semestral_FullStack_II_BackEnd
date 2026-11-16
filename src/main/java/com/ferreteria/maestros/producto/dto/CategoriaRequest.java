package com.ferreteria.maestros.producto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(

        @NotBlank(message = "El nombre de la categoría es obligatorio.")
        @Size(max = 80, message = "El nombre no puede superar los 80 caracteres.")
        String nombre,

        @Size(max = 255, message = "La ruta de imagen no puede superar los 255 caracteres.")
        String imagenUrl
) {
}
