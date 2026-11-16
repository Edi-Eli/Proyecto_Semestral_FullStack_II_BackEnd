package com.ferreteria.maestros.producto.mapper;

import com.ferreteria.maestros.producto.dto.ProductoAdminDTO;
import com.ferreteria.maestros.producto.dto.ProductoDTO;
import com.ferreteria.maestros.producto.dto.ProductoRequest;
import com.ferreteria.maestros.producto.entity.Producto;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    @Mapping(target = "precio", source = "precioVenta")
    @Mapping(target = "stockCritico", source = "stockMinimo")
    @Mapping(target = "imagen", source = "imagenUrl")
    @Mapping(target = "categoriaId", source = "subcategoria.categoria.id")
    @Mapping(target = "categoriaNombre", source = "subcategoria.categoria.nombre")
    @Mapping(target = "subcategoriaId", source = "subcategoria.id")
    @Mapping(target = "subcategoriaNombre", source = "subcategoria.nombre")
    ProductoDTO aDTO(Producto producto);

    List<ProductoDTO> aDTOs(List<Producto> productos);

    @Mapping(target = "precio", source = "precioVenta")
    @Mapping(target = "stockCritico", source = "stockMinimo")
    @Mapping(target = "imagen", source = "imagenUrl")
    @Mapping(target = "categoriaId", source = "subcategoria.categoria.id")
    @Mapping(target = "categoriaNombre", source = "subcategoria.categoria.nombre")
    @Mapping(target = "subcategoriaId", source = "subcategoria.id")
    @Mapping(target = "subcategoriaNombre", source = "subcategoria.nombre")
    ProductoAdminDTO aAdminDTO(Producto producto);

    List<ProductoAdminDTO> aAdminDTOs(List<Producto> productos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subcategoria", ignore = true)
    @Mapping(target = "precioVenta", source = "precio")
    @Mapping(target = "stockMinimo", source = "stockCritico")
    @Mapping(target = "imagenUrl", source = "imagen")
    @Mapping(target = "enOferta", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    Producto aEntidad(ProductoRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subcategoria", ignore = true)
    @Mapping(target = "precioVenta", source = "precio")
    @Mapping(target = "stockMinimo", source = "stockCritico")
    @Mapping(target = "imagenUrl", source = "imagen")
    @Mapping(target = "enOferta", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    void actualizar(ProductoRequest request, @MappingTarget Producto producto);
}
