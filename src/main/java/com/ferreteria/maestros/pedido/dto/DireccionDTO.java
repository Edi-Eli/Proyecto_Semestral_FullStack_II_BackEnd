package com.ferreteria.maestros.pedido.dto;

public record DireccionDTO(
        String calle,
        String depto,
        String region,
        String comuna,
        String indicaciones
) {
}