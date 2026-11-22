package com.ferreteria.maestros.contacto.mapper;

import com.ferreteria.maestros.contacto.dto.MensajeContactoDTO;
import com.ferreteria.maestros.contacto.entity.MensajeContacto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Mapper MapStruct del módulo contacto. */
@Mapper(componentModel = "spring")
public interface MensajeContactoMapper {

    /** {@code fechaCreacion} -> {@code fecha} (nombre que usa el front). */
    @Mapping(target = "fecha", source = "fechaCreacion")
    MensajeContactoDTO aDTO(MensajeContacto mensaje);

    List<MensajeContactoDTO> aDTOs(List<MensajeContacto> mensajes);
}
