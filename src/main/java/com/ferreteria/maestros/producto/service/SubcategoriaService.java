package com.ferreteria.maestros.producto.service;

import com.ferreteria.maestros.common.exception.RecursoDuplicadoException;
import com.ferreteria.maestros.common.exception.RecursoNoEncontradoException;
import com.ferreteria.maestros.common.exception.ReglaNegocioException;
import com.ferreteria.maestros.producto.dto.SubcategoriaDTO;
import com.ferreteria.maestros.producto.dto.SubcategoriaRequest;
import com.ferreteria.maestros.producto.entity.Categoria;
import com.ferreteria.maestros.producto.entity.Subcategoria;
import com.ferreteria.maestros.producto.mapper.SubcategoriaMapper;
import com.ferreteria.maestros.producto.repository.ProductoRepository;
import com.ferreteria.maestros.producto.repository.SubcategoriaRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubcategoriaService {

    private static final Logger log = LoggerFactory.getLogger(SubcategoriaService.class);

    private final SubcategoriaRepository subcategoriaRepository;
    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;
    private final SubcategoriaMapper subcategoriaMapper;

    public SubcategoriaService(SubcategoriaRepository subcategoriaRepository,
                               ProductoRepository productoRepository,
                               CategoriaService categoriaService,
                               SubcategoriaMapper subcategoriaMapper) {
        this.subcategoriaRepository = subcategoriaRepository;
        this.productoRepository = productoRepository;
        this.categoriaService = categoriaService;
        this.subcategoriaMapper = subcategoriaMapper;
    }

    @Transactional(readOnly = true)
    public List<SubcategoriaDTO> listar(Long categoriaId) {
        List<Subcategoria> subcategorias = categoriaId == null
                ? subcategoriaRepository.findAllByOrderByNombreAsc()
                : subcategoriaRepository.findByCategoriaIdOrderByNombreAsc(categoriaId);
        return subcategoriaMapper.aDTOs(subcategorias);
    }

    @Transactional(readOnly = true)
    public SubcategoriaDTO obtener(Long id) {
        return subcategoriaMapper.aDTO(buscarEntidad(id));
    }

    @Transactional
    public SubcategoriaDTO crear(SubcategoriaRequest request) {
        Categoria categoria = categoriaService.buscarEntidad(request.categoriaId());
        String nombre = request.nombre().trim();
        if (subcategoriaRepository.existsByCategoriaIdAndNombreIgnoreCase(categoria.getId(), nombre)) {
            throw new RecursoDuplicadoException(
                    "Ya existe una subcategoría '" + nombre + "' en la categoría " + categoria.getNombre() + ".");
        }
        Subcategoria subcategoria = subcategoriaMapper.aEntidad(request);
        subcategoria.setCategoria(categoria);
        subcategoria.setNombre(nombre);
        Subcategoria guardada = subcategoriaRepository.save(subcategoria);
        log.info("Subcategoría creada: '{}' (categoría '{}')", guardada.getNombre(), categoria.getNombre());
        return subcategoriaMapper.aDTO(guardada);
    }

    @Transactional
    public SubcategoriaDTO actualizar(Long id, SubcategoriaRequest request) {
        Subcategoria subcategoria = buscarEntidad(id);
        Categoria categoria = categoriaService.buscarEntidad(request.categoriaId());
        String nombre = request.nombre().trim();
        boolean mismaCategoria = subcategoria.getCategoria().getId().equals(categoria.getId());
        if ((!mismaCategoria || !subcategoria.getNombre().equalsIgnoreCase(nombre))
                && subcategoriaRepository.existsByCategoriaIdAndNombreIgnoreCase(categoria.getId(), nombre)) {
            throw new RecursoDuplicadoException(
                    "Ya existe una subcategoría '" + nombre + "' en la categoría " + categoria.getNombre() + ".");
        }
        subcategoriaMapper.actualizar(request, subcategoria);
        subcategoria.setCategoria(categoria);
        subcategoria.setNombre(nombre);
        Subcategoria guardada = subcategoriaRepository.save(subcategoria);
        log.info("Subcategoría actualizada: '{}'", guardada.getNombre());
        return subcategoriaMapper.aDTO(guardada);
    }

    @Transactional
    public void eliminar(Long id) {
        Subcategoria subcategoria = buscarEntidad(id);
        if (productoRepository.existsBySubcategoriaId(id)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar la subcategoría '" + subcategoria.getNombre() + "' porque tiene productos asociados.");
        }
        subcategoriaRepository.delete(subcategoria);
        log.info("Subcategoría eliminada: '{}'", subcategoria.getNombre());
    }

    Subcategoria buscarEntidad(Long id) {
        return subcategoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una subcategoría con id " + id + "."));
    }
}
