package com.ferreteria.maestros.pedido.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ferreteria.maestros.common.exception.RecursoNoEncontradoException;
import com.ferreteria.maestros.common.exception.ReglaNegocioException;
import com.ferreteria.maestros.cuenta.port.CuentaCorrientePort;
import com.ferreteria.maestros.pedido.dto.CheckoutRequest;
import com.ferreteria.maestros.pedido.dto.DireccionRequest;
import com.ferreteria.maestros.pedido.dto.ItemPedidoRequest;
import com.ferreteria.maestros.pedido.entity.EstadoPedido;
import com.ferreteria.maestros.pedido.entity.MetodoPago;
import com.ferreteria.maestros.pedido.entity.Pedido;
import com.ferreteria.maestros.pedido.entity.PedidoItem;
import com.ferreteria.maestros.pedido.entity.TipoEntrega;
import com.ferreteria.maestros.pedido.mapper.PedidoMapper;
import com.ferreteria.maestros.pedido.repository.PedidoRepository;
import com.ferreteria.maestros.producto.dto.ProductoDTO;
import com.ferreteria.maestros.producto.port.StockServicePort;
import com.ferreteria.maestros.producto.service.ProductoService;
import com.ferreteria.maestros.usuario.entity.Usuario;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    private static final Long PRODUCTO = 1L;
    private static final Long USUARIO_ID = 7L;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PedidoMapper pedidoMapper;

    @Mock
    private ProductoService productoService;

    @Mock
    private StockServicePort stockService;

    @Mock
    private CuentaCorrientePort cuentaCorriente;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(
                pedidoRepository, pedidoMapper, productoService, stockService, cuentaCorriente);
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(USUARIO_ID);
        usuario.setNombre("Maria");
        usuario.setApellidos("Torres Silva");
        usuario.setCorreo("maria.torres@gmail.com");
        return usuario;
    }

    private ProductoDTO producto(String precio, String precioOferta, boolean enOferta, int stock) {
        return new ProductoDTO(PRODUCTO, "CON001", "Cemento", "desc", "Polpaico", "Saco",
                new BigDecimal(precio), precioOferta == null ? null : new BigDecimal(precioOferta),
                enOferta, stock, 5, "/img.png", 1L, "Mat", 1L, "Cementos", true);
    }

    private CheckoutRequest checkout(MetodoPago metodoPago) {
        return new CheckoutRequest(
                List.of(new ItemPedidoRequest(PRODUCTO, 2)),
                new DireccionRequest("Calle 1", "", "RM", "Santiago", ""),
                TipoEntrega.DESPACHO,
                metodoPago);
    }

    @Test
    void checkoutDescuentaStockYGuardaPedido() {
        when(productoService.obtener(PRODUCTO)).thenReturn(producto("1000", null, false, 10));
        when(stockService.hayStock(PRODUCTO, 2)).thenReturn(true);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        pedidoService.checkout(usuario(), checkout(MetodoPago.CONTADO));

        verify(stockService).descontarStock(Map.of(PRODUCTO, 2));

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());
        Pedido pedido = captor.getValue();
        assertEquals(new BigDecimal("2000"), pedido.getTotal());
        assertEquals(EstadoPedido.APROBADA, pedido.getEstado());
        assertEquals(1, pedido.getItems().size());
    }

    @Test
    void checkoutSinStockLanzaReglaDeNegocio() {
        when(productoService.obtener(PRODUCTO)).thenReturn(producto("1000", null, false, 1));
        when(stockService.hayStock(PRODUCTO, 2)).thenReturn(false);

        assertThrows(ReglaNegocioException.class,
                () -> pedidoService.checkout(usuario(), checkout(MetodoPago.CONTADO)));

        verify(stockService, never()).descontarStock(any());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void checkoutConCuentaCorrienteCargaLaCuenta() {
        when(productoService.obtener(PRODUCTO)).thenReturn(producto("1000", null, false, 10));
        when(stockService.hayStock(PRODUCTO, 2)).thenReturn(true);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        pedidoService.checkout(usuario(), checkout(MetodoPago.CUENTA_CORRIENTE));

        verify(cuentaCorriente).validarYRegistrarCargo(USUARIO_ID, new BigDecimal("2000"), 1L);

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());
        assertEquals(EstadoPedido.PENDIENTE, captor.getValue().getEstado());
    }

    @Test
    void rechazarPedidoReponeStock() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.APROBADA);
        PedidoItem item = new PedidoItem();
        item.setProductoId(PRODUCTO);
        item.setCantidad(2);
        pedido.agregarItem(item);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        pedidoService.cambiarEstado(1L, EstadoPedido.RECHAZADA);

        verify(stockService).reponerStock(Map.of(PRODUCTO, 2));
        assertEquals(EstadoPedido.RECHAZADA, pedido.getEstado());
    }

    @Test
    void pedidoInexistenteLanzaRecursoNoEncontrado() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> pedidoService.detalle(99L));
    }
}