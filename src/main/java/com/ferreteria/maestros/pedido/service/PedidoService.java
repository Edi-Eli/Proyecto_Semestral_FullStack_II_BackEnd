package com.ferreteria.maestros.pedido.service;

import com.ferreteria.maestros.common.exception.RecursoNoEncontradoException;
import com.ferreteria.maestros.common.exception.ReglaNegocioException;
import com.ferreteria.maestros.cuenta.port.CuentaCorrientePort;
import com.ferreteria.maestros.pedido.dto.CheckoutRequest;
import com.ferreteria.maestros.pedido.dto.DireccionRequest;
import com.ferreteria.maestros.pedido.dto.ItemPedidoRequest;
import com.ferreteria.maestros.pedido.dto.PedidoDetalleDTO;
import com.ferreteria.maestros.pedido.dto.PedidoResumenDTO;
import com.ferreteria.maestros.pedido.entity.EstadoPedido;
import com.ferreteria.maestros.pedido.entity.MetodoPago;
import com.ferreteria.maestros.pedido.entity.Pedido;
import com.ferreteria.maestros.pedido.entity.PedidoItem;
import com.ferreteria.maestros.pedido.mapper.PedidoMapper;
import com.ferreteria.maestros.pedido.port.PedidoQueryPort;
import com.ferreteria.maestros.pedido.repository.PedidoRepository;
import com.ferreteria.maestros.producto.dto.ProductoDTO;
import com.ferreteria.maestros.producto.port.StockServicePort;
import com.ferreteria.maestros.producto.service.ProductoService;
import com.ferreteria.maestros.usuario.entity.Usuario;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService implements PedidoQueryPort {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);
    private static final DateTimeFormatter FORMATO_NRO = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;
    private final ProductoService productoService;
    private final StockServicePort stockService;
    private final CuentaCorrientePort cuentaCorriente;

    public PedidoService(PedidoRepository pedidoRepository,
                         PedidoMapper pedidoMapper,
                         ProductoService productoService,
                         StockServicePort stockService,
                         CuentaCorrientePort cuentaCorriente) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoMapper = pedidoMapper;
        this.productoService = productoService;
        this.stockService = stockService;
        this.cuentaCorriente = cuentaCorriente;
    }

    @Transactional
    public PedidoDetalleDTO checkout(Usuario usuario, CheckoutRequest request) {
        // 1) Armar items (snapshot) y acumular cantidades por producto
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        List<PedidoItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedidoRequest linea : request.items()) {
            ProductoDTO producto = productoService.obtener(linea.productoId());
            int cantidad = linea.cantidad();

            if (!stockService.hayStock(producto.id(), cantidad)) {
                throw new ReglaNegocioException("Stock insuficiente para '" + producto.nombre()
                        + "'. Disponible: " + producto.stock() + ".");
            }

            BigDecimal precio = precioEfectivo(producto);
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cantidad));

            PedidoItem item = new PedidoItem();
            item.setProductoId(producto.id());
            item.setNombre(producto.nombre());
            item.setPrecioUnitario(precio);
            item.setCantidad(cantidad);
            item.setSubtotal(subtotal);
            items.add(item);

            cantidades.merge(producto.id(), cantidad, Integer::sum);
            total = total.add(subtotal);
        }

        stockService.descontarStock(cantidades);

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setCodigo(generarCodigo());
        pedido.setNro(LocalDateTime.now(ZoneOffset.UTC).format(FORMATO_NRO));
        pedido.setClienteNombre(usuario.getNombre());
        pedido.setClienteApellidos(usuario.getApellidos());
        pedido.setClienteCorreo(usuario.getCorreo());

        DireccionRequest dir = request.direccion();
        pedido.setDireccionCalle(dir.calle());
        pedido.setDireccionDepto(dir.depto());
        pedido.setRegion(dir.region());
        pedido.setComuna(dir.comuna());
        pedido.setIndicaciones(dir.indicaciones());

        pedido.setTipoEntrega(request.tipoEntrega());
        pedido.setMetodoPago(request.metodoPago());
        pedido.setEstado(request.metodoPago() == MetodoPago.CUENTA_CORRIENTE
                ? EstadoPedido.PENDIENTE
                : EstadoPedido.APROBADA);
        pedido.setTotal(total);
        items.forEach(pedido::agregarItem);

        Pedido guardado = pedidoRepository.save(pedido);

        if (request.metodoPago() == MetodoPago.CUENTA_CORRIENTE) {
            cuentaCorriente.validarYRegistrarCargo(usuario.getId(), total, guardado.getId());
        }

        log.info("Pedido {} creado por '{}' por un total de {}", guardado.getCodigo(), usuario.getCorreo(), total);
        return pedidoMapper.aDetalle(guardado);
    }

    @Transactional(readOnly = true)
    public Page<PedidoResumenDTO> listar(EstadoPedido estado, Pageable pageable) {
        Page<Pedido> pagina = (estado == null)
                ? pedidoRepository.findAll(pageable)
                : pedidoRepository.findByEstado(estado, pageable);
        return pagina.map(pedidoMapper::aResumen);
    }

    @Transactional(readOnly = true)
    public List<PedidoResumenDTO> mios(Long usuarioId) {
        return pedidoMapper.aResumenes(
                pedidoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId));
    }

    @Transactional(readOnly = true)
    public List<PedidoResumenDTO> porUsuario(Long usuarioId) {
        return mios(usuarioId);
    }

    @Transactional(readOnly = true)
    public PedidoDetalleDTO detalle(Long id) {
        return pedidoMapper.aDetalle(buscarEntidad(id));
    }

    @Transactional(readOnly = true)
    public PedidoDetalleDTO boleta(Long id) {
        return detalle(id);
    }

    @Transactional
    public PedidoDetalleDTO cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = buscarEntidad(id);

        if (nuevoEstado == EstadoPedido.RECHAZADA && pedido.getEstado() != EstadoPedido.RECHAZADA) {
            Map<Long, Integer> cantidades = new LinkedHashMap<>();
            pedido.getItems().forEach(item ->
                    cantidades.merge(item.getProductoId(), item.getCantidad(), Integer::sum));
            stockService.reponerStock(cantidades);
            log.info("Pedido {} rechazado: stock repuesto.", pedido.getCodigo());
        }

        pedido.setEstado(nuevoEstado);
        return pedidoMapper.aDetalle(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResumenDTO> historialPorUsuario(Long usuarioId) {
        return pedidoMapper.aResumenes(
                pedidoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId));
    }






    private BigDecimal precioEfectivo(ProductoDTO producto) {
        if (producto.enOferta() && producto.precioOferta() != null
                && producto.precioOferta().compareTo(BigDecimal.ZERO) > 0) {
            return producto.precioOferta();
        }
        return producto.precio();
    }

    private String generarCodigo() {
        return "ORDER" + System.currentTimeMillis();
    }

    private Pedido buscarEntidad(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un pedido con id " + id + "."));
    }
}