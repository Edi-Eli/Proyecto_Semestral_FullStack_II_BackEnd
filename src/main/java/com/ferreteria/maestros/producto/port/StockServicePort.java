package com.ferreteria.maestros.producto.port;

import java.util.Map;

public interface StockServicePort {

    boolean hayStock(Long productoId, int cantidad);

    void descontarStock(Map<Long, Integer> cantidades);

    void reponerStock(Map<Long, Integer> cantidades);
}
