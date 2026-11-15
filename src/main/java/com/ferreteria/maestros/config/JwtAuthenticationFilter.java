package com.ferreteria.maestros.config;

import com.ferreteria.maestros.auth.service.JwtService;
import com.ferreteria.maestros.usuario.entity.Usuario;
import com.ferreteria.maestros.usuario.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro único de la aplicación: si la petición trae {@code Authorization: Bearer <jwt>},
 * valida el token, carga el usuario desde la base de datos y deja su autenticación
 * (con la authority {@code ROLE_<ROL>}) en el {@code SecurityContext}.
 *
 * <p>Se instancia desde {@link SecurityConfig} (no es un {@code @Component}) para evitar que
 * Spring Boot lo registre dos veces como filtro de servlet.</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String CABECERA = "Authorization";
    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String cabecera = request.getHeader(CABECERA);
        if (cabecera != null && cabecera.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = cabecera.substring(PREFIJO.length()).trim();
            jwtService.extraerCorreo(token)
                    .flatMap(usuarioRepository::findByCorreo)
                    .filter(Usuario::isActivo)
                    .ifPresent(usuario -> autenticar(request, usuario));
        }
        filterChain.doFilter(request, response);
    }

    private void autenticar(HttpServletRequest request, Usuario usuario) {
        String autoridad = "ROLE_" + usuario.getRol().getNombre().toUpperCase(Locale.ROOT);
        var authorities = List.of(new SimpleGrantedAuthority(autoridad));
        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Autenticado '{}' con authority {}", usuario.getCorreo(), autoridad);
    }
}
