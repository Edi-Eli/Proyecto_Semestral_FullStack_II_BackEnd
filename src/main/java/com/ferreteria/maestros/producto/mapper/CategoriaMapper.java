package com.ferreteria.maestros.producto.mapper;

import com.ferreteria.maestros.producto.dto.CategoriaDTO;
import com.ferreteria.maestros.producto.dto.CategoriaRequest;
import com.ferreteria.maestros.producto.entity.Categoria;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    @Mapping(target = "imagen", source = "imagenUrl")
    CategoriaDTO aDTO(Categoria categoria);

    List<CategoriaDTO> aDTOs(List<Categoria> categorias);

    @Mapping(target = "id", ignore = true)
    Categoria aEntidad(CategoriaRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void actualizar(CategoriaRequest request, @MappingTarget Categoria categoria);
}
