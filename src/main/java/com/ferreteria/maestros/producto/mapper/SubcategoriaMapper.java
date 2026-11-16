package com.ferreteria.maestros.producto.mapper;

import com.ferreteria.maestros.producto.dto.SubcategoriaDTO;
import com.ferreteria.maestros.producto.dto.SubcategoriaRequest;
import com.ferreteria.maestros.producto.entity.Subcategoria;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SubcategoriaMapper {

    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    SubcategoriaDTO aDTO(Subcategoria subcategoria);

    List<SubcategoriaDTO> aDTOs(List<Subcategoria> subcategorias);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    Subcategoria aEntidad(SubcategoriaRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    void actualizar(SubcategoriaRequest request, @MappingTarget Subcategoria subcategoria);
}
