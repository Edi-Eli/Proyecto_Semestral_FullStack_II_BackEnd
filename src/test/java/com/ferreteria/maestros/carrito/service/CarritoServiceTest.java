// Carrito (CarritoService) — realizado por: Sebas
package com.ferreteria.maestros.carrito.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ferreteria.maestros.carrito.entity.Carrito;
import com.ferreteria.maestros.carrito.entity.CarritoItem;
import com.ferreteria.maestros.carrito.mapper.CarritoMapper;
import com.ferreteria.maestros.carrito.repository.CarritoRepository;
import com.ferreteria.maestros.common.exception.ReglaNegocioException;
import com.ferreteria.maestros.producto.dto.ProductoDTO;
import com.ferreteria.maestros.producto.service.ProductoService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    private static final Long USUARIO = 7L;
    private static final Long PRODUCTO = 1L;

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductoService productoService;

    @Mock
    private CarritoMapper carritoMapper;

    @InjectMocks
    private CarritoService carritoService;

    private ProductoDTO producto(BigDecimal precio, BigDecimal precioOferta, boolean enOferta, int stock) {
        return new ProductoDTO(PRODUCTO, "CON001", "Cemento", "desc", "Polpaico", "Saco",
                precio, precioOferta, enOferta, stock, 5, "/img.png", 1L, "Mat", 1L, "Cementos", true);
    }

    private Carrito carritoConItem() {
        Carrito carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUsuarioId(USUARIO);
        CarritoItem item = new CarritoItem();
        item.setProductoId(PRODUCTO);
        item.setNombre("Cemento");
        item.setPrecioUnitario(new BigDecimal("1000"));
        item.setCantidad(2);
        item.setCarrito(carrito);
        carrito.getItems().add(item);
        return carrito;
    }

    @Test
    void agregarSinStockSuficienteLanzaReglaDeNegocio() {
        when(productoService.obtener(PRODUCTO)).thenReturn(producto(new BigDecimal("1000"), null, false, 2));
        when(carritoRepository.findByUsuarioId(USUARIO)).thenReturn(Optional.empty());
        when(productoService.hayStock(PRODUCTO, 5)).thenReturn(false);

        assertThrows(ReglaNegocioException.class, () -> carritoService.agregar(USUARIO, PRODUCTO, 5));

        verify(carritoRepository, never()).save(any());
    }

    @Test
    void agregarGuardaSnapshotConPrecioDeOferta() {
        when(productoService.obtener(PRODUCTO))
                .thenReturn(producto(new BigDecimal("1000"), new BigDecimal("800"), true, 10));
        when(carritoRepository.findByUsuarioId(USUARIO)).thenReturn(Optional.empty());
        when(productoService.hayStock(PRODUCTO, 2)).thenReturn(true);
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        carritoService.agregar(USUARIO, PRODUCTO, 2);

        ArgumentCaptor<Carrito> captor = ArgumentCaptor.forClass(Carrito.class);
        verify(carritoRepository).save(captor.capture());
        CarritoItem item = captor.getValue().getItems().get(0);
        assertEquals(new BigDecimal("800"), item.getPrecioUnitario());
        assertEquals(2, item.getCantidad());
        assertEquals("Cemento", item.getNombre());
        assertEquals("/img.png", item.getImagenUrl());
    }

    @Test
    void actualizarCantidadCeroEliminaElItem() {
        Carrito carrito = carritoConItem();
        when(carritoRepository.findByUsuarioId(USUARIO)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        carritoService.actualizarCantidad(USUARIO, PRODUCTO, 0);

        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    void vaciarEliminaTodosLosItems() {
        Carrito carrito = carritoConItem();
        when(carritoRepository.findByUsuarioId(USUARIO)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        carritoService.vaciar(USUARIO);

        assertTrue(carrito.getItems().isEmpty());
    }
}
