package com.ferreteria.maestros.producto.repository;

import com.ferreteria.maestros.producto.entity.Subcategoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubcategoriaRepository extends JpaRepository<Subcategoria, Long> {

    List<Subcategoria> findByCategoriaIdOrderByNombreAsc(Long categoriaId);

    List<Subcategoria> findAllByOrderByNombreAsc();

    boolean existsByCategoriaId(Long categoriaId);

    boolean existsByCategoriaIdAndNombreIgnoreCase(Long categoriaId, String nombre);
}
