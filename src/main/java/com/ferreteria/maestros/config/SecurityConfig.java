package com.ferreteria.maestros.config;

import com.ferreteria.maestros.auth.service.JwtService;
import com.ferreteria.maestros.usuario.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración única de seguridad del monolito (JWT + roles, sin sesión de servidor).
 *
 * <p>Matriz de rutas: ver el comentario de {@code authorizeHttpRequests}. Las reglas finas
 * (Administrador o dueño) se aplican con {@code @PreAuthorize} en los controladores;
 * aquí se define el mínimo por ruta.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    public SecurityConfig(JwtService jwtService,
                          UsuarioRepository usuarioRepository,
                          RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                          RestAccessDeniedHandler restAccessDeniedHandler) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
    }

    /** Codificador de contraseñas (BCrypt). Se usa en el login, el registro y el seed. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(jwtService, usuarioRepository);

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basico -> basico.disable())
                .logout(salir -> salir.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // --- Infraestructura / documentación (públicas) ---
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()

                        // --- Públicas de negocio ---
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/productos/**", "/api/categorias/**", "/api/subcategorias/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/contactos").permitAll()

                        // --- Solo Administrador ---
                        .requestMatchers("/api/roles/**").hasRole("ADMINISTRADOR")
                        // Usuarios: el control fino (Administrador o el propio dueño) se hace con
                        // @PreAuthorize en UsuarioController; aquí solo se exige estar autenticado.
                        .requestMatchers("/api/usuarios/**").authenticated()
                        // Reportes: el detalle por método (@PreAuthorize) decide cuáles son solo ADMIN.
                        .requestMatchers("/api/reportes/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/productos/**", "/api/categorias/**", "/api/subcategorias/**")
                        .hasRole("ADMINISTRADOR")

                        // --- Panel compartido Administrador / Vendedor ---
                        // Productos: el Vendedor puede crear, editar y ajustar stock.
                        .requestMatchers(HttpMethod.POST, "/api/productos/**")
                        .hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**")
                        .hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/productos/**")
                        .hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                        // Categorías y subcategorías: solo Administrador (coincide con sus @PreAuthorize).
                        .requestMatchers(HttpMethod.POST, "/api/categorias/**", "/api/subcategorias/**")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/**", "/api/subcategorias/**")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/categorias/**", "/api/subcategorias/**")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/contactos/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/contactos/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR")

                        // --- Resto: cualquier usuario autenticado ---
                        // (carrito, pedidos, cuentas y /api/auth/sesion | /logout; el detalle
                        //  «Administrador o dueño» lo aplican los controladores con @PreAuthorize)
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
