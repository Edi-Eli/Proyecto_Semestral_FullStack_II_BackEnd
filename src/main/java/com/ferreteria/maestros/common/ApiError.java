package com.ferreteria.maestros.common;

import java.time.LocalDateTime;

public record ApiError(
        LocalDateTime timestamp,
        int codigo,
        String mensaje,
        String ruta
) {
    public static ApiError de(int codigo, String mensaje, String ruta) {
        return new ApiError(LocalDateTime.now(), codigo, mensaje, ruta);
    }
}
