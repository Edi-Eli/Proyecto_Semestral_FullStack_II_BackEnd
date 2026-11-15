package com.ferreteria.maestros.auth.controller;

import com.ferreteria.maestros.auth.dto.LoginRequest;
import com.ferreteria.maestros.auth.dto.LoginResponse;
import com.ferreteria.maestros.auth.dto.RegistroRequest;
import com.ferreteria.maestros.auth.dto.SesionResponse;
import com.ferreteria.maestros.auth.service.AuthService;
import com.ferreteria.maestros.usuario.entity.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de autenticación. Todo el flujo es <b>stateless</b> con JWT.
 *
 * <ul>
 *   <li>{@code POST /api/auth/login} — público.</li>
 *   <li>{@code POST /api/auth/registro} — público (crea rol Cliente).</li>
 *   <li>{@code GET  /api/auth/sesion} (alias {@code /validar}) — autenticado.</li>
 *   <li>{@code POST /api/auth/logout} — autenticado (el cliente descarta el token).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Inicio de sesión, registro de clientes y sesión actual (JWT).")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            description = "Valida las credenciales (BCrypt) y devuelve un JWT en { token, tipo, usuario }.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos."),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas."),
            @ApiResponse(responseCode = "403", description = "La cuenta está desactivada.")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar cliente",
            description = "Crea un usuario con rol Cliente y devuelve 201 con un JWT (auto-login).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos."),
            @ApiResponse(responseCode = "409", description = "Correo o RUN ya registrados."),
            @ApiResponse(responseCode = "422", description = "RUN inválido.")
    })
    public ResponseEntity<LoginResponse> registro(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registro(request));
    }

    @GetMapping({"/sesion", "/validar"})
    @Operation(summary = "Sesión actual",
            description = "Devuelve el usuario del token. Lo usa el front al recargar la aplicación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión válida."),
            @ApiResponse(responseCode = "401", description = "Sin token o token inválido.")
    })
    public ResponseEntity<SesionResponse> sesion(Authentication authentication) {
        Usuario usuario = autenticado(authentication);
        return ResponseEntity.ok(new SesionResponse(authService.sesionDe(usuario)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión",
            description = "Stateless: el servidor no guarda estado; el cliente descarta el token. Responde 204.")
    @ApiResponse(responseCode = "204", description = "Sesión cerrada en el cliente.")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    private Usuario autenticado(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Usuario usuario) {
            return usuario;
        }
        throw new AuthenticationCredentialsNotFoundException("No hay una sesión autenticada.");
    }
}
