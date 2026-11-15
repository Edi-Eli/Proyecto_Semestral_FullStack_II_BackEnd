package com.ferreteria.maestros.usuario.mapper;

import com.ferreteria.maestros.usuario.dto.RolDTO;
import com.ferreteria.maestros.usuario.dto.UsuarioActualizarRequest;
import com.ferreteria.maestros.usuario.dto.UsuarioCrearRequest;
import com.ferreteria.maestros.usuario.dto.UsuarioDTO;
import com.ferreteria.maestros.usuario.entity.Rol;
import com.ferreteria.maestros.usuario.entity.Usuario;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper MapStruct del módulo usuario (implementación generada en tiempo de compilación).
 * Nunca expone {@code passwordHash}.
 */
@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    /** Entidad -> DTO público. {@code tipoUsuario} se toma del nombre del rol. */
    @Mapping(target = "tipoUsuario", source = "rol.nombre")
    UsuarioDTO aDTO(Usuario usuario);

    List<UsuarioDTO> aDTOs(List<Usuario> usuarios);

    /** Rol -> DTO. */
    RolDTO aRolDTO(Rol rol);

    List<RolDTO> aRolDTOs(List<Rol> roles);

    /**
     * {@link UsuarioCrearRequest} -> {@link Usuario}.
     * El rol, el hash de contraseña, el estado y las marcas de tiempo los fija el servicio.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "cuentaCorriente", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    Usuario aEntidad(UsuarioCrearRequest request);

    /**
     * Copia los campos editables del request sobre una entidad existente.
     * Se ignoran los nulos para no borrar datos que el cliente no envía
     * (p. ej. teléfono o preferencia de entrega).
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "run", ignore = true)
    @Mapping(target = "correo", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "cuentaCorriente", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    void actualizar(UsuarioActualizarRequest request, @MappingTarget Usuario usuario);
}
