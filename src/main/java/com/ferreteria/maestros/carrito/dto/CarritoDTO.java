package com.ferreteria.maestros.carrito.dto;

import java.util.List;

public record CarritoDTO(
        Long id,
        List<CarritoItemDTO> items
) {
}
