// Catálogo (ProductoService) — realizado por: Sebas
package com.ferreteria.maestros.producto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ferreteria.maestros.common.exception.ReglaNegocioException;
import com.ferreteria.maestros.producto.dto.ActualizarStockRequest;
import com.ferreteria.maestros.producto.entity.Producto;
import com.ferreteria.maestros.producto.mapper.ProductoMapper;
import com.ferreteria.maestros.producto.repository.ProductoRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private SubcategoriaService subcategoriaService;

    @Mock
    private ProductoMapper productoMapper;

    @InjectMocks
    private ProductoService productoService;

    private Producto productoConStock(int stock) {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setCodigo("CON001");
        producto.setNombre("Cemento");
        producto.setStock(stock);
        return producto;
    }

    @Test
    void hayStockDevuelveTrueCuandoAlcanza() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoConStock(10)));

        assertTrue(productoService.hayStock(1L, 5));
    }

    @Test
    void hayStockDevuelveFalseCuandoNoExiste() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertFalse(productoService.hayStock(99L, 1));
    }

    @Test
    void retirarMasDelStockLanzaReglaDeNegocio() {
        Producto producto = productoConStock(5);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertThrows(ReglaNegocioException.class,
                () -> productoService.actualizarStock(1L, new ActualizarStockRequest(10, "RETIRAR")));

        verify(productoRepository, never()).save(any());
    }

    @Test
    void agregarStockIncrementaLaCantidad() {
        Producto producto = productoConStock(5);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        productoService.actualizarStock(1L, new ActualizarStockRequest(3, "AGREGAR"));

        assertEquals(8, producto.getStock());
    }

    @Test
    void descontarStockInsuficienteLanzaReglaDeNegocio() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoConStock(2)));

        assertThrows(ReglaNegocioException.class,
                () -> productoService.descontarStock(Map.of(1L, 5)));
    }

    @Test
    void descontarStockReduceLaCantidad() {
        Producto producto = productoConStock(10);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        productoService.descontarStock(Map.of(1L, 4));

        assertEquals(6, producto.getStock());
    }
}
