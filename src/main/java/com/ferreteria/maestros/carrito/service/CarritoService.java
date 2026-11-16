package com.ferreteria.maestros.carrito.service;

import com.ferreteria.maestros.carrito.dto.CarritoDTO;
import com.ferreteria.maestros.carrito.dto.CarritoItemDTO;
import com.ferreteria.maestros.carrito.entity.Carrito;
import com.ferreteria.maestros.carrito.entity.CarritoItem;
import com.ferreteria.maestros.carrito.mapper.CarritoMapper;
import com.ferreteria.maestros.carrito.repository.CarritoRepository;
import com.ferreteria.maestros.common.exception.RecursoNoEncontradoException;
import com.ferreteria.maestros.common.exception.ReglaNegocioException;
import com.ferreteria.maestros.producto.dto.ProductoDTO;
import com.ferreteria.maestros.producto.service.ProductoService;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarritoService {

    private static final Logger log = LoggerFactory.getLogger(CarritoService.class);

    private final CarritoRepository carritoRepository;
    private final ProductoService productoService;
    private final CarritoMapper carritoMapper;

    public CarritoService(CarritoRepository carritoRepository,
                          ProductoService productoService,
                          CarritoMapper carritoMapper) {
        this.carritoRepository = carritoRepository;
        this.productoService = productoService;
        this.carritoMapper = carritoMapper;
    }

    @Transactional(readOnly = true)
    public CarritoDTO obtener(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId)
                .map(this::aDTO)
                .orElseGet(() -> new CarritoDTO(null, List.of()));
    }

    @Transactional
    public CarritoDTO agregar(Long usuarioId, Long productoId, int cantidad) {
        ProductoDTO producto = productoService.obtener(productoId);
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId).orElseGet(() -> nuevoCarrito(usuarioId));

        CarritoItem item = carrito.getItems().stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst()
                .orElse(null);

        int cantidadNueva = (item == null ? 0 : item.getCantidad()) + cantidad;
        validarStock(producto, cantidadNueva);

        if (item == null) {
            item = new CarritoItem();
            item.setCarrito(carrito);
            item.setProductoId(productoId);
            item.setNombre(producto.nombre());
            item.setImagenUrl(producto.imagen());
            carrito.getItems().add(item);
        }
        item.setCantidad(cantidadNueva);
        item.setPrecioUnitario(precioEfectivo(producto));

        Carrito guardado = carritoRepository.save(carrito);
        log.info("Carrito de usuario {}: producto {} en cantidad {}", usuarioId, productoId, cantidadNueva);
        return aDTO(guardado);
    }

    @Transactional
    public CarritoDTO actualizarCantidad(Long usuarioId, Long productoId, int cantidad) {
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId).orElse(null);
        if (carrito == null) {
            return new CarritoDTO(null, List.of());
        }

        CarritoItem item = carrito.getItems().stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst()
                .orElse(null);
        if (item == null) {
            return aDTO(carrito);
        }

        if (cantidad <= 0) {
            carrito.getItems().remove(item);
        } else {
            ProductoDTO producto = productoService.obtener(productoId);
            validarStock(producto, cantidad);
            item.setCantidad(cantidad);
            item.setPrecioUnitario(precioEfectivo(producto));
        }

        return aDTO(carritoRepository.save(carrito));
    }

    @Transactional
    public CarritoDTO eliminar(Long usuarioId, Long productoId) {
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId).orElse(null);
        if (carrito == null) {
            return new CarritoDTO(null, List.of());
        }
        carrito.getItems().removeIf(i -> i.getProductoId().equals(productoId));
        return aDTO(carritoRepository.save(carrito));
    }

    @Transactional
    public CarritoDTO vaciar(Long usuarioId) {
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId).orElse(null);
        if (carrito == null) {
            return new CarritoDTO(null, List.of());
        }
        carrito.getItems().clear();
        return aDTO(carritoRepository.save(carrito));
    }

    private void validarStock(ProductoDTO producto, int cantidad) {
        if (!productoService.hayStock(producto.id(), cantidad)) {
            throw new ReglaNegocioException(
                    "Solo hay " + producto.stock() + " unidades disponibles de '" + producto.nombre() + "'.");
        }
    }

    private BigDecimal precioEfectivo(ProductoDTO producto) {
        if (producto.enOferta() && producto.precioOferta() != null
                && producto.precioOferta().compareTo(BigDecimal.ZERO) > 0) {
            return producto.precioOferta();
        }
        return producto.precio();
    }

    private Carrito nuevoCarrito(Long usuarioId) {
        Carrito carrito = new Carrito();
        carrito.setUsuarioId(usuarioId);
        return carrito;
    }

    private CarritoDTO aDTO(Carrito carrito) {
        List<CarritoItemDTO> items = carrito.getItems().stream()
                .map(item -> carritoMapper.aItemDTO(item, stockActual(item.getProductoId())))
                .toList();
        return carritoMapper.aDTO(carrito, items);
    }

    private Integer stockActual(Long productoId) {
        try {
            return productoService.obtener(productoId).stock();
        } catch (RecursoNoEncontradoException ex) {
            return 0;
        }
    }
}
