package com.ferreteria.maestros.producto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductoRequest(

        @NotBlank(message = "El código es obligatorio.")
        @Size(max = 20, message = "El código no puede superar los 20 caracteres.")
        String codigo,

        @NotNull(message = "Debe indicar la subcategoría.")
        Long subcategoriaId,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
        String nombre,

        String descripcion,

        @Size(max = 80, message = "La marca no puede superar los 80 caracteres.")
        String marca,

        @Size(max = 30, message = "La unidad no puede superar los 30 caracteres.")
        String unidad,

        @NotNull(message = "El precio de compra es obligatorio.")
        @DecimalMin(value = "0.0", message = "El precio de compra no puede ser negativo.")
        BigDecimal precioCompra,

        @NotNull(message = "El precio es obligatorio.")
        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo.")
        BigDecimal precio,

        Boolean enOferta,

        @DecimalMin(value = "0.0", message = "El precio de oferta no puede ser negativo.")
        BigDecimal precioOferta,

        @NotNull(message = "El stock es obligatorio.")
        @Min(value = 0, message = "El stock no puede ser negativo.")
        Integer stock,

        @NotNull(message = "El stock crítico es obligatorio.")
        @Min(value = 0, message = "El stock crítico no puede ser negativo.")
        Integer stockCritico,

        @Size(max = 500, message = "La ruta de imagen no puede superar los 500 caracteres.")
        String imagen,

        Boolean activo
) {
}
