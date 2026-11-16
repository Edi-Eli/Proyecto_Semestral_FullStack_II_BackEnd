package com.ferreteria.maestros.producto.service;

import com.ferreteria.maestros.common.exception.RecursoDuplicadoException;
import com.ferreteria.maestros.common.exception.RecursoNoEncontradoException;
import com.ferreteria.maestros.common.exception.ReglaNegocioException;
import com.ferreteria.maestros.producto.dto.CategoriaDTO;
import com.ferreteria.maestros.producto.dto.CategoriaRequest;
import com.ferreteria.maestros.producto.entity.Categoria;
import com.ferreteria.maestros.producto.mapper.CategoriaMapper;
import com.ferreteria.maestros.producto.repository.CategoriaRepository;
import com.ferreteria.maestros.producto.repository.SubcategoriaRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaService.class);

    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaService(CategoriaRepository categoriaRepository,
                            SubcategoriaRepository subcategoriaRepository,
                            CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.subcategoriaRepository = subcategoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listar() {
        return categoriaMapper.aDTOs(categoriaRepository.findAll());
    }

    @Transactional(readOnly = true)
    public CategoriaDTO obtener(Long id) {
        return categoriaMapper.aDTO(buscarEntidad(id));
    }

    @Transactional
    public CategoriaDTO crear(CategoriaRequest request) {
        String nombre = request.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new RecursoDuplicadoException("Ya existe una categoría con el nombre " + nombre + ".");
        }
        Categoria categoria = categoriaMapper.aEntidad(request);
        categoria.setNombre(nombre);
        Categoria guardada = categoriaRepository.save(categoria);
        log.info("Categoría creada: '{}'", guardada.getNombre());
        return categoriaMapper.aDTO(guardada);
    }

    @Transactional
    public CategoriaDTO actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarEntidad(id);
        String nombre = request.nombre().trim();
        if (!categoria.getNombre().equalsIgnoreCase(nombre)
                && categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new RecursoDuplicadoException("Ya existe otra categoría con el nombre " + nombre + ".");
        }
        categoriaMapper.actualizar(request, categoria);
        categoria.setNombre(nombre);
        Categoria guardada = categoriaRepository.save(categoria);
        log.info("Categoría actualizada: '{}'", guardada.getNombre());
        return categoriaMapper.aDTO(guardada);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscarEntidad(id);
        if (subcategoriaRepository.existsByCategoriaId(id)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar la categoría '" + categoria.getNombre() + "' porque tiene subcategorías asociadas.");
        }
        categoriaRepository.delete(categoria);
        log.info("Categoría eliminada: '{}'", categoria.getNombre());
    }

    Categoria buscarEntidad(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una categoría con id " + id + "."));
    }
}
