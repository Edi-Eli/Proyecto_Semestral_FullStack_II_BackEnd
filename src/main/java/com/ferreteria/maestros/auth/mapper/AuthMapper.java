package com.ferreteria.maestros.auth.mapper;

import com.ferreteria.maestros.auth.dto.RegistroRequest;
import com.ferreteria.maestros.auth.dto.UsuarioSesionDTO;
import com.ferreteria.maestros.usuario.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct entre DTOs de autenticación y la entidad {@link Usuario}.
 * La implementación la genera MapStruct en tiempo de compilación
 * (bean de Spring: {@code authMapperImpl}).
 */
@Mapper(componentModel = "spring")
public interface AuthMapper {

    /**
     * Convierte un {@link RegistroRequest} en {@link Usuario}.
     * Los campos que no vienen del request (rol, hash de contraseña, estado y marcas de tiempo)
     * se ignoran aquí y los asigna {@code AuthService}.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "cuentaCorriente", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    Usuario aEntidad(RegistroRequest request);

    /** Usuario -> DTO de sesión (sin contraseña). {@code tipoUsuario} se toma del rol. */
    @Mapping(target = "tipoUsuario", source = "rol.nombre")
    UsuarioSesionDTO aSesionDTO(Usuario usuario);
}
