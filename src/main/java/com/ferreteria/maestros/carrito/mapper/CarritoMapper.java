package com.ferreteria.maestros.carrito.mapper;

import com.ferreteria.maestros.carrito.dto.CarritoDTO;
import com.ferreteria.maestros.carrito.dto.CarritoItemDTO;
import com.ferreteria.maestros.carrito.entity.Carrito;
import com.ferreteria.maestros.carrito.entity.CarritoItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarritoMapper {

    @Mapping(target = "precio", source = "item.precioUnitario")
    @Mapping(target = "imagen", source = "item.imagenUrl")
    CarritoItemDTO aItemDTO(CarritoItem item, Integer stock);

    @Mapping(target = "items", source = "items")
    CarritoDTO aDTO(Carrito carrito, List<CarritoItemDTO> items);
}
