package com.ferreteria.maestros.config;

import com.ferreteria.maestros.common.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Responde <b>401</b> con el cuerpo estándar {@link ApiError} cuando una petición
 * no está autenticada (sin token o token inválido/expirado).
 *
 * <p>Es clave para el front: {@code src/api/client.js} hace {@code logout()} automático
 * al recibir un 401.</p>
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ApiError error = ApiError.de(
                HttpStatus.UNAUTHORIZED.value(),
                "No autenticado: inicie sesión o su sesión ha expirado.",
                request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
