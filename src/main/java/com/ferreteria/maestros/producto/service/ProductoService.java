package com.ferreteria.maestros.producto.service;

import com.ferreteria.maestros.common.exception.RecursoDuplicadoException;
import com.ferreteria.maestros.common.exception.RecursoNoEncontradoException;
import com.ferreteria.maestros.common.exception.ReglaNegocioException;
import com.ferreteria.maestros.producto.dto.ActualizarStockRequest;
import com.ferreteria.maestros.producto.dto.ProductoAdminDTO;
import com.ferreteria.maestros.producto.dto.ProductoDTO;
import com.ferreteria.maestros.producto.dto.ProductoRequest;
import com.ferreteria.maestros.producto.entity.Producto;
import com.ferreteria.maestros.producto.entity.Subcategoria;
import com.ferreteria.maestros.producto.mapper.ProductoMapper;
import com.ferreteria.maestros.producto.port.StockServicePort;
import com.ferreteria.maestros.producto.repository.ProductoRepository;
import com.ferreteria.maestros.producto.repository.ProductoSpecifications;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService implements StockServicePort {

    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository productoRepository;
    private final SubcategoriaService subcategoriaService;
    private final ProductoMapper productoMapper;

    public ProductoService(ProductoRepository productoRepository,
                           SubcategoriaService subcategoriaService,
                           ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.subcategoriaService = subcategoriaService;
        this.productoMapper = productoMapper;
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listar(String q, Long categoriaId, Long subcategoriaId,
                                    Boolean enOferta, Pageable pageable) {
        return buscar(q, categoriaId, subcategoriaId, enOferta, Boolean.TRUE, pageable)
                .map(productoMapper::aDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductoAdminDTO> listarAdmin(String q, Long categoriaId, Long subcategoriaId,
                                              Boolean enOferta, Pageable pageable) {
        return buscar(q, categoriaId, subcategoriaId, enOferta, null, pageable)
                .map(productoMapper::aAdminDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarOfertas(Pageable pageable) {
        Specification<Producto> spec = Specification.allOf(
                ProductoSpecifications.conActivo(true),
                ProductoSpecifications.conEnOferta(true),
                ProductoSpecifications.conPrecioOfertaPositivo());
        return productoRepository.findAll(spec, pageable).map(productoMapper::aDTO);
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarCriticos() {
        Specification<Producto> spec = Specification.allOf(
                ProductoSpecifications.conActivo(true),
                ProductoSpecifications.conStockCritico());
        return productoMapper.aDTOs(productoRepository.findAll(spec));
    }

    @Transactional(readOnly = true)
    public ProductoDTO obtener(Long id) {
        return productoMapper.aDTO(buscarEntidad(id));
    }

    @Transactional(readOnly = true)
    public ProductoAdminDTO obtenerAdmin(Long id) {
        return productoMapper.aAdminDTO(buscarEntidad(id));
    }

    @Transactional
    public ProductoAdminDTO crear(ProductoRequest request) {
        String codigo = normalizarCodigo(request.codigo());
        if (productoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new RecursoDuplicadoException("Ya existe un producto con el código " + codigo + ".");
        }
        Subcategoria subcategoria = subcategoriaService.buscarEntidad(request.subcategoriaId());

        Producto producto = productoMapper.aEntidad(request);
        producto.setCodigo(codigo);
        producto.setSubcategoria(subcategoria);
        producto.setEnOferta(Boolean.TRUE.equals(request.enOferta()));
        producto.setActivo(request.activo() == null || request.activo());
        normalizarOferta(producto);

        Producto guardado = productoRepository.save(producto);
        log.info("Producto creado: '{}' ({})", guardado.getNombre(), guardado.getCodigo());
        return productoMapper.aAdminDTO(guardado);
    }

    @Transactional
    public ProductoAdminDTO actualizar(Long id, ProductoRequest request) {
        Producto producto = buscarEntidad(id);
        String codigo = normalizarCodigo(request.codigo());
        if (!producto.getCodigo().equalsIgnoreCase(codigo)
                && productoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new RecursoDuplicadoException("Ya existe otro producto con el código " + codigo + ".");
        }
        Subcategoria subcategoria = subcategoriaService.buscarEntidad(request.subcategoriaId());

        productoMapper.actualizar(request, producto);
        producto.setCodigo(codigo);
        producto.setSubcategoria(subcategoria);
        if (request.enOferta() != null) {
            producto.setEnOferta(request.enOferta());
        }
        if (request.activo() != null) {
            producto.setActivo(request.activo());
        }
        normalizarOferta(producto);

        Producto guardado = productoRepository.save(producto);
        log.info("Producto actualizado: '{}' ({})", guardado.getNombre(), guardado.getCodigo());
        return productoMapper.aAdminDTO(guardado);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscarEntidad(id);
        producto.setActivo(false);
        productoRepository.save(producto);
        log.info("Producto '{}' eliminado lógicamente (activo=false).", producto.getCodigo());
    }

    @Transactional
    public ProductoAdminDTO actualizarStock(Long id, ActualizarStockRequest request) {
        Producto producto = buscarEntidad(id);
        int cantidad = request.cantidad();
        if ("RETIRAR".equalsIgnoreCase(request.accion())) {
            if (producto.getStock() < cantidad) {
                throw new ReglaNegocioException(
                        "Stock insuficiente para '" + producto.getNombre() + "'. Disponible: " + producto.getStock() + ".");
            }
            producto.setStock(producto.getStock() - cantidad);
        } else {
            producto.setStock(producto.getStock() + cantidad);
        }
        Producto guardado = productoRepository.save(producto);
        log.info("Stock de '{}' actualizado a {}", guardado.getCodigo(), guardado.getStock());
        return productoMapper.aAdminDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hayStock(Long productoId, int cantidad) {
        return productoRepository.findById(productoId)
                .map(producto -> producto.getStock() >= cantidad)
                .orElse(false);
    }

    @Override
    @Transactional
    public void descontarStock(Map<Long, Integer> cantidades) {
        cantidades.forEach((productoId, cantidad) -> {
            Producto producto = buscarEntidad(productoId);
            if (producto.getStock() < cantidad) {
                throw new ReglaNegocioException(
                        "Stock insuficiente para '" + producto.getNombre() + "'. Disponible: " + producto.getStock() + ".");
            }
            producto.setStock(producto.getStock() - cantidad);
            productoRepository.save(producto);
        });
    }

    @Override
    @Transactional
    public void reponerStock(Map<Long, Integer> cantidades) {
        cantidades.forEach((productoId, cantidad) -> {
            Producto producto = buscarEntidad(productoId);
            producto.setStock(producto.getStock() + cantidad);
            productoRepository.save(producto);
        });
    }

    private Page<Producto> buscar(String q, Long categoriaId, Long subcategoriaId,
                                  Boolean enOferta, Boolean activo, Pageable pageable) {
        List<Specification<Producto>> filtros = new ArrayList<>();
        if (activo != null) {
            filtros.add(ProductoSpecifications.conActivo(activo));
        }
        if (q != null && !q.isBlank()) {
            filtros.add(ProductoSpecifications.conTexto(q.trim()));
        }
        if (categoriaId != null) {
            filtros.add(ProductoSpecifications.conCategoria(categoriaId));
        }
        if (subcategoriaId != null) {
            filtros.add(ProductoSpecifications.conSubcategoria(subcategoriaId));
        }
        if (enOferta != null) {
            filtros.add(ProductoSpecifications.conEnOferta(enOferta));
        }
        Specification<Producto> especificacion = filtros.isEmpty()
                ? Specification.unrestricted()
                : Specification.allOf(filtros);
        return productoRepository.findAll(especificacion, pageable);
    }

    private void normalizarOferta(Producto producto) {
        if (!producto.isEnOferta()
                || producto.getPrecioOferta() == null
                || producto.getPrecioOferta().compareTo(BigDecimal.ZERO) <= 0) {
            producto.setPrecioOferta(null);
        }
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    Producto buscarEntidad(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto con id " + id + "."));
    }
}
