package com.ferreteria.maestros.pedido.mapper;

import com.ferreteria.maestros.pedido.dto.ClienteDTO;
import com.ferreteria.maestros.pedido.dto.DireccionDTO;
import com.ferreteria.maestros.pedido.dto.PedidoDetalleDTO;
import com.ferreteria.maestros.pedido.dto.PedidoItemDTO;
import com.ferreteria.maestros.pedido.dto.PedidoResumenDTO;
import com.ferreteria.maestros.pedido.entity.Pedido;
import com.ferreteria.maestros.pedido.entity.PedidoItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PedidoMapper {

    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "fecha", source = "fechaCreacion")
    @Mapping(target = "estado", expression = "java(pedido.getEstado().name())")
    @Mapping(target = "tipoEntrega", expression = "java(pedido.getTipoEntrega().name())")
    @Mapping(target = "metodoPago", expression = "java(pedido.getMetodoPago().name())")
    @Mapping(target = "cliente", expression = "java(clienteDe(pedido))")
    @Mapping(target = "direccion", expression = "java(direccionDe(pedido))")
    PedidoDetalleDTO aDetalle(Pedido pedido);

    @Mapping(target = "fecha", source = "fechaCreacion")
    @Mapping(target = "estado", expression = "java(pedido.getEstado().name())")
    @Mapping(target = "tipoEntrega", expression = "java(pedido.getTipoEntrega().name())")
    @Mapping(target = "metodoPago", expression = "java(pedido.getMetodoPago().name())")
    PedidoResumenDTO aResumen(Pedido pedido);

    List<PedidoResumenDTO> aResumenes(List<Pedido> pedidos);

    @Mapping(target = "precio", source = "precioUnitario")
    PedidoItemDTO aItemDTO(PedidoItem item);







    default ClienteDTO clienteDe(Pedido pedido) {
        return new ClienteDTO(pedido.getClienteNombre(), pedido.getClienteApellidos(), pedido.getClienteCorreo());
    }

    default DireccionDTO direccionDe(Pedido pedido) {
        return new DireccionDTO(
                pedido.getDireccionCalle(),
                pedido.getDireccionDepto(),
                pedido.getRegion(),
                pedido.getComuna(),
                pedido.getIndicaciones());
    }
}