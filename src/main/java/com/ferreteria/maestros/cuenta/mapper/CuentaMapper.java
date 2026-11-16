package com.ferreteria.maestros.cuenta.mapper;

import com.ferreteria.maestros.cuenta.dto.CuentaResumenDTO;
import com.ferreteria.maestros.cuenta.dto.MovimientoDTO;
import com.ferreteria.maestros.cuenta.entity.CuentaCorriente;
import com.ferreteria.maestros.cuenta.entity.MovimientoCuenta;
import com.ferreteria.maestros.usuario.entity.Usuario;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CuentaMapper {

    @Mapping(target = "clienteId", source = "cliente.id")
    @Mapping(target = "clienteNombre", expression = "java(nombreCompleto(cuenta.getCliente()))")
    @Mapping(target = "disponible", expression = "java(cuenta.getDisponible())")
    CuentaResumenDTO aResumen(CuentaCorriente cuenta);

    @Mapping(target = "tipo", expression = "java(movimiento.getTipo().name())")
    MovimientoDTO aMovimientoDTO(MovimientoCuenta movimiento);

    List<MovimientoDTO> aMovimientoDTOs(List<MovimientoCuenta> movimientos);

    default String nombreCompleto(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return (usuario.getNombre() + " " + usuario.getApellidos()).trim();
    }
}