package com.ferreteria.maestros.usuario.service;

import com.ferreteria.maestros.common.exception.RecursoDuplicadoException;
import com.ferreteria.maestros.common.exception.RecursoNoEncontradoException;
import com.ferreteria.maestros.common.exception.ReglaNegocioException;
import com.ferreteria.maestros.common.util.RutUtils;
import com.ferreteria.maestros.pedido.dto.PedidoResumenDTO;
import com.ferreteria.maestros.pedido.port.PedidoQueryPort;
import com.ferreteria.maestros.usuario.dto.RolDTO;
import com.ferreteria.maestros.usuario.dto.UsuarioActualizarRequest;
import com.ferreteria.maestros.usuario.dto.UsuarioCrearRequest;
import com.ferreteria.maestros.usuario.dto.UsuarioDTO;
import com.ferreteria.maestros.usuario.entity.Rol;
import com.ferreteria.maestros.usuario.entity.Usuario;
import com.ferreteria.maestros.usuario.mapper.UsuarioMapper;
import com.ferreteria.maestros.usuario.repository.RolRepository;
import com.ferreteria.maestros.usuario.repository.UsuarioRepository;
import com.ferreteria.maestros.usuario.repository.UsuarioSpecifications;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica del módulo usuario: listado con filtros, CRUD, activar/desactivar e historial de compras.
 */
@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    /**
     * Contraseña que se asigna cuando un administrador crea un usuario sin indicar una.
     * (La vista {@code AdminUsuarios} del front todavía no envía el campo contraseña.)
     */
    private static final String PASSWORD_POR_DEFECTO = "1234";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;
    private final ObjectProvider<PedidoQueryPort> pedidoQueryPort;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder,
                          UsuarioMapper usuarioMapper,
                          ObjectProvider<PedidoQueryPort> pedidoQueryPort) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioMapper = usuarioMapper;
        this.pedidoQueryPort = pedidoQueryPort;
    }

    /** Listado paginado con filtros opcionales. */
    @Transactional(readOnly = true)
    public Page<UsuarioDTO> listar(String rol, Boolean activo, String q, Pageable pageable) {
        String rolFiltro = limpiarFiltro(rol);
        String textoFiltro = limpiarFiltro(q);

        List<Specification<Usuario>> filtros = new ArrayList<>();
        if (rolFiltro != null) {
            filtros.add(UsuarioSpecifications.conRol(rolFiltro));
        }
        if (activo != null) {
            filtros.add(UsuarioSpecifications.conActivo(activo));
        }
        if (textoFiltro != null) {
            filtros.add(UsuarioSpecifications.conTexto(textoFiltro));
        }

        Specification<Usuario> especificacion = filtros.isEmpty()
                ? Specification.unrestricted()
                : Specification.allOf(filtros);

        return usuarioRepository.findAll(especificacion, pageable).map(usuarioMapper::aDTO);
    }

    /** Detalle de un usuario por RUN. */
    @Transactional(readOnly = true)
    public UsuarioDTO obtener(String run) {
        return usuarioMapper.aDTO(buscarEntidad(run));
    }

    /** Catálogo de roles. */
    @Transactional(readOnly = true)
    public List<RolDTO> listarRoles() {
        return usuarioMapper.aRolDTOs(rolRepository.findAll());
    }

    /**
     * Historial de compras de un usuario.
     *
     * <p>Usa el contrato {@link PedidoQueryPort}, implementado por el módulo pedido (Zeus, Fase 6).
     * Mientras no exista implementación, devuelve una lista vacía para no bloquear el módulo.</p>
     */
    @Transactional(readOnly = true)
    public List<PedidoResumenDTO> historial(String run) {
        Usuario usuario = buscarEntidad(run);
        PedidoQueryPort puerto = pedidoQueryPort.getIfAvailable();
        if (puerto == null) {
            log.warn("El módulo de pedidos aún no está disponible; historial vacío para RUN {}", run);
            return List.of();
        }
        return puerto.historialPorUsuario(usuario.getId());
    }

    /** Crea un usuario (cualquier rol). */
    @Transactional
    public UsuarioDTO crear(UsuarioCrearRequest request) {
        if (!RutUtils.esValido(request.run())) {
            throw new ReglaNegocioException("El RUN ingresado no es válido.");
        }
        String run = RutUtils.normalizar(request.run());
        String correo = normalizarCorreo(request.correo());

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new RecursoDuplicadoException("Ya existe un usuario con el correo " + correo + ".");
        }
        if (usuarioRepository.existsByRun(run)) {
            throw new RecursoDuplicadoException("Ya existe un usuario con el RUN " + run + ".");
        }

        Rol rol = resolverRol(request.tipoUsuario());

        Usuario usuario = usuarioMapper.aEntidad(request);
        usuario.setRun(run);
        usuario.setCorreo(correo);
        usuario.setRol(rol);
        usuario.setActivo(request.activo() == null || request.activo());
        usuario.setCuentaCorriente(request.cuentaCorriente() != null && request.cuentaCorriente());

        if (request.password() == null || request.password().isBlank()) {
            log.warn("Usuario '{}' creado sin contraseña; se asignó la contraseña por defecto.", correo);
            usuario.setPasswordHash(passwordEncoder.encode(PASSWORD_POR_DEFECTO));
        } else {
            usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario creado: '{}' con rol '{}'", guardado.getCorreo(), rol.getNombre());
        return usuarioMapper.aDTO(guardado);
    }

    /**
     * Actualiza un usuario. Un Administrador puede cambiar el rol, el estado y la cuenta
     * corriente; el dueño de la cuenta (si no es administrador) solo sus datos de perfil.
     */
    @Transactional
    public UsuarioDTO actualizar(String run, UsuarioActualizarRequest request, boolean esAdmin) {
        Usuario usuario = buscarEntidad(run);
        String correo = normalizarCorreo(request.correo());

        if (!usuario.getCorreo().equalsIgnoreCase(correo) && usuarioRepository.existsByCorreo(correo)) {
            throw new RecursoDuplicadoException("Ya existe otro usuario con el correo " + correo + ".");
        }

        usuarioMapper.actualizar(request, usuario);
        usuario.setCorreo(correo);

        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        if (esAdmin) {
            if (request.tipoUsuario() != null && !request.tipoUsuario().isBlank()) {
                usuario.setRol(resolverRol(request.tipoUsuario()));
            }
            if (request.activo() != null) {
                usuario.setActivo(request.activo());
            }
            if (request.cuentaCorriente() != null) {
                usuario.setCuentaCorriente(request.cuentaCorriente());
            }
        } else if (request.tipoUsuario() != null || request.activo() != null || request.cuentaCorriente() != null) {
            log.warn("El usuario '{}' intentó cambiar rol/estado/cuenta corriente sin ser administrador; se ignoró.",
                    usuario.getCorreo());
        }

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario actualizado: '{}'", guardado.getCorreo());
        return usuarioMapper.aDTO(guardado);
    }

    /** Activa o desactiva un usuario. */
    @Transactional
    public UsuarioDTO cambiarEstado(String run, boolean activo) {
        Usuario usuario = buscarEntidad(run);
        usuario.setActivo(activo);
        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario '{}' {}", guardado.getCorreo(), activo ? "activado" : "desactivado");
        return usuarioMapper.aDTO(guardado);
    }

    /**
     * Elimina un usuario. Se implementa como <b>baja lógica</b> ({@code activo = false}),
     * que es lo recomendado en el documento 02: conserva las referencias de pedidos y
     * cuentas corrientes.
     */
    @Transactional
    public void eliminar(String run) {
        Usuario usuario = buscarEntidad(run);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        log.info("Usuario '{}' eliminado lógicamente (activo=false).", usuario.getCorreo());
    }

    /* ------------------------- utilidades internas ------------------------- */

    private Usuario buscarEntidad(String run) {
        String normalizado = RutUtils.normalizar(run);
        return usuarioRepository.findByRun(normalizado)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con RUN " + run + "."));
    }

    private Rol resolverRol(String tipoUsuario) {
        return rolRepository.findByNombreIgnoreCase(tipoUsuario.trim())
                .orElseThrow(() -> new ReglaNegocioException(
                        "El tipo de usuario '" + tipoUsuario + "' no existe."));
    }

    private String normalizarCorreo(String correo) {
        return correo == null ? null : correo.trim().toLowerCase(Locale.ROOT);
    }

    private String limpiarFiltro(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
