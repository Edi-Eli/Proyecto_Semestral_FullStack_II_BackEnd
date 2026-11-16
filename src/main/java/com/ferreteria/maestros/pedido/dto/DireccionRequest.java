package com.ferreteria.maestros.pedido.dto;

import jakarta.validation.constraints.Size;

public record DireccionRequest(

        @Size(max = 300, message = "La calle no puede superar los 300 caracteres.")
        String calle,

        @Size(max = 100, message = "El depto no puede superar los 100 caracteres.")
        String depto,

        @Size(max = 100, message = "La región no puede superar los 100 caracteres.")
        String region,

        @Size(max = 100, message = "La comuna no puede superar los 100 caracteres.")
        String comuna,

        @Size(max = 300, message = "Las indicaciones no pueden superar los 300 caracteres.")
        String indicaciones
) {
}