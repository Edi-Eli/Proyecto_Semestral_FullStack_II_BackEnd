package com.ferreteria.maestros.common;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
        List<T> contenido,
        int pagina,
        int tamanio,
        long totalElementos,
        int totalPaginas,
        boolean ultima
) {

    /** Construye la respuesta directamente desde una {@link Page} de Spring Data. */
    public static <T> PageResponse<T> desde(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    /** Construye la respuesta desde una {@link Page} original y su contenido ya mapeado a DTO. */
    public static <T> PageResponse<T> desde(Page<?> page, List<T> contenido) {
        return new PageResponse<>(
                contenido,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
