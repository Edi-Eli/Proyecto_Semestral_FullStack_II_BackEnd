package com.ferreteria.maestros.auth.service;

import com.ferreteria.maestros.auth.dto.LoginRequest;
import com.ferreteria.maestros.auth.dto.LoginResponse;
import com.ferreteria.maestros.auth.dto.RegistroRequest;
import com.ferreteria.maestros.auth.dto.UsuarioSesionDTO;
import com.ferreteria.maestros.auth.mapper.AuthMapper;
import com.ferreteria.maestros.common.exception.RecursoDuplicadoException;
import com.ferreteria.maestros.common.exception.ReglaNegocioException;
import com.ferreteria.maestros.common.util.RutUtils;
import com.ferreteria.maestros.usuario.entity.Rol;
import com.ferreteria.maestros.usuario.entity.Usuario;
import com.ferreteria.maestros.usuario.repository.RolRepository;
import com.ferreteria.maestros.usuario.repository.UsuarioRepository;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de autenticación: login (BCrypt + JWT), registro de clientes y sesión actual.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String ROL_CLIENTE = "Cliente";
    private static final String TIPO_TOKEN = "Bearer";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMapper authMapper;

    public AuthService(UsuarioRepository usuarioRepository,
                       RolRepository rolRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthMapper authMapper) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
    }

    /** Valida credenciales y devuelve {@code { token, tipo, usuario }}. */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String correo = normalizarCorreo(request.correo());

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new BadCredentialsException("Correo o contraseña incorrectos."));

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            log.warn("Intento de login fallido para '{}'", correo);
            throw new BadCredentialsException("Correo o contraseña incorrectos.");
        }
        if (!usuario.isActivo()) {
            log.warn("Login rechazado: la cuenta '{}' está desactivada", correo);
            throw new DisabledException("La cuenta está desactivada. Contacte al administrador.");
        }

        log.info("Login exitoso de '{}' (rol {})", usuario.getCorreo(), usuario.getRol().getNombre());
        return new LoginResponse(jwtService.generarToken(usuario), TIPO_TOKEN, authMapper.aSesionDTO(usuario));
    }

    /** Registra un usuario nuevo con rol Cliente y devuelve su token (auto-login). */
    @Transactional
    public LoginResponse registro(RegistroRequest request) {
        String correo = normalizarCorreo(request.correo());

        if (!RutUtils.esValido(request.run())) {
            throw new ReglaNegocioException("El RUN ingresado no es válido.");
        }
        String run = RutUtils.normalizar(request.run());

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new RecursoDuplicadoException("Ya existe un usuario registrado con el correo " + correo + ".");
        }
        if (usuarioRepository.existsByRun(run)) {
            throw new RecursoDuplicadoException("Ya existe un usuario registrado con el RUN " + run + ".");
        }

        Rol rolCliente = rolRepository.findByNombre(ROL_CLIENTE)
                .orElseThrow(() -> new ReglaNegocioException(
                        "El rol 'Cliente' no está configurado en el sistema."));

        Usuario usuario = authMapper.aEntidad(request);
        usuario.setCorreo(correo);
        usuario.setRun(run);
        usuario.setRol(rolCliente);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setActivo(true);
        usuario.setCuentaCorriente(false);

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario registrado: '{}' con rol '{}'", guardado.getCorreo(), rolCliente.getNombre());

        return new LoginResponse(jwtService.generarToken(guardado), TIPO_TOKEN, authMapper.aSesionDTO(guardado));
    }

    /** Datos de sesión del usuario autenticado. */
    @Transactional(readOnly = true)
    public UsuarioSesionDTO sesionDe(Usuario usuario) {
        return authMapper.aSesionDTO(usuario);
    }

    private String normalizarCorreo(String correo) {
        return correo == null ? null : correo.trim().toLowerCase(Locale.ROOT);
    }
}
