package com.ferreteria.maestros.common;

import com.ferreteria.maestros.common.exception.RecursoDuplicadoException;
import com.ferreteria.maestros.common.exception.RecursoNoEncontradoException;
import com.ferreteria.maestros.common.exception.ReglaNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 404 — recurso inexistente. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(RecursoNoEncontradoException ex,
                                                        HttpServletRequest request) {
        log.warn("Recurso no encontrado en {}: {}", request.getRequestURI(), ex.getMessage());
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /** 422 — regla de negocio incumplida (no es un error de formato). */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiError> manejarReglaNegocio(ReglaNegocioException ex,
                                                        HttpServletRequest request) {
        log.warn("Regla de negocio en {}: {}", request.getRequestURI(), ex.getMessage());
        // Spring Framework 7 renombró UNPROCESSABLE_ENTITY -> UNPROCESSABLE_CONTENT (RFC 9110).
        return construir(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request);
    }

    /** 409 — recurso ya existente (p. ej. correo o RUN duplicados). */
    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ApiError> manejarDuplicado(RecursoDuplicadoException ex,
                                                     HttpServletRequest request) {
        log.warn("Recurso duplicado en {}: {}", request.getRequestURI(), ex.getMessage());
        return construir(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /** 400 — errores de Bean Validation sobre un {@code @RequestBody}. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(MethodArgumentNotValidException ex,
                                                      HttpServletRequest request) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (detalle.isBlank()) {
            detalle = "Datos inválidos en la solicitud.";
        }
        log.warn("Validación fallida en {}: {}", request.getRequestURI(), detalle);
        return construir(HttpStatus.BAD_REQUEST, detalle, request);
    }

    /** 400 — violaciones de restricciones en parámetros ({@code @RequestParam}, {@code @PathVariable}). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> manejarConstraintViolation(ConstraintViolationException ex,
                                                               HttpServletRequest request) {
        String detalle = ex.getConstraintViolations().stream()
                .map(violacion -> violacion.getPropertyPath() + ": " + violacion.getMessage())
                .collect(Collectors.joining("; "));
        if (detalle.isBlank()) {
            detalle = "Parámetros inválidos en la solicitud.";
        }
        log.warn("Restricción violada en {}: {}", request.getRequestURI(), detalle);
        return construir(HttpStatus.BAD_REQUEST, detalle, request);
    }

    /** 400 — JSON ilegible o campo con tipo incorrecto. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> manejarCuerpoIlegible(HttpMessageNotReadableException ex,
                                                          HttpServletRequest request) {
        log.warn("Cuerpo de solicitud ilegible en {}: {}", request.getRequestURI(), ex.getMessage());
        return construir(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es un JSON válido o tiene un tipo incorrecto.", request);
    }

    /** 400 — parámetro de ruta/consulta con tipo incompatible. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> manejarTipoParametro(MethodArgumentTypeMismatchException ex,
                                                         HttpServletRequest request) {
        String mensaje = "El parámetro '" + ex.getName() + "' tiene un valor no válido.";
        log.warn("Tipo de parámetro inválido en {}: {}", request.getRequestURI(), mensaje);
        return construir(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    /** 409 — violación de integridad referencial o de unicidad. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> manejarIntegridad(DataIntegrityViolationException ex,
                                                      HttpServletRequest request) {
        log.warn("Conflicto de integridad en {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return construir(HttpStatus.CONFLICT,
                "La operación entra en conflicto con datos existentes (posible duplicado o referencia inválida).",
                request);
    }

    /** 401 — credenciales incorrectas en el login. */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> manejarCredenciales(BadCredentialsException ex,
                                                       HttpServletRequest request) {
        log.warn("Credenciales inválidas en {}: {}", request.getRequestURI(), ex.getMessage());
        return construir(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    /** 403 — la cuenta existe pero está desactivada. */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> manejarCuentaDesactivada(DisabledException ex,
                                                            HttpServletRequest request) {
        log.warn("Cuenta desactivada en {}: {}", request.getRequestURI(), ex.getMessage());
        return construir(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    /** 401 — cualquier otro problema de autenticación no controlado explícitamente. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> manejarAutenticacion(AuthenticationException ex,
                                                        HttpServletRequest request) {
        log.warn("Error de autenticación en {}: {}", request.getRequestURI(), ex.getMessage());
        return construir(HttpStatus.UNAUTHORIZED, "No autenticado: inicie sesión o su sesión ha expirado.", request);
    }

    /** 403 — usuario autenticado pero sin el rol/permiso requerido. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> manejarAccesoDenegado(AccessDeniedException ex,
                                                          HttpServletRequest request) {
        log.warn("Acceso denegado en {}: {}", request.getRequestURI(), ex.getMessage());
        return construir(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta acción.", request);
    }

    /**
     * Último recurso: cualquier error no controlado explícitamente.
     *
     * <p>Si la excepción implementa {@link ErrorResponse} —la mayoría de las excepciones de
     * Spring MVC (ruta inexistente, metodo no permitido, tipo de contenido no soportado, etc.)—
     * se respeta su código <b>4xx</b> en lugar de devolver siempre un 500.</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarGenerico(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse errorResponse && errorResponse.getStatusCode().is4xxClientError()) {
            int codigo = errorResponse.getStatusCode().value();
            log.warn("Solicitud rechazada en {} ({}): {}", request.getRequestURI(), codigo, ex.getMessage());
            return construir(HttpStatus.valueOf(codigo), mensajeParaEstado(codigo), request);
        }
        log.error("Error inesperado en {}", request.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno. Intente nuevamente más tarde.", request);
    }

    private String mensajeParaEstado(int codigo) {
        return switch (codigo) {
            case 404 -> "El recurso solicitado no existe.";
            case 405 -> "El método HTTP no está permitido para esta ruta.";
            case 406 -> "El formato de respuesta solicitado no está disponible.";
            case 415 -> "El tipo de contenido enviado no está soportado.";
            default -> "La solicitud no pudo ser procesada.";
        };
    }

    private ResponseEntity<ApiError> construir(HttpStatus estado, String mensaje, HttpServletRequest request) {
        ApiError error = ApiError.de(estado.value(), mensaje, request.getRequestURI());
        return ResponseEntity.status(estado).body(error);
    }
}
