package com.ferreteria.maestros.auth.service;

import com.ferreteria.maestros.usuario.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Genera y valida los JWT (HS256). La clave y la expiración se leen de
 * {@code app.jwt.secret} y {@code app.jwt.expiration-ms} (ver application.yaml).
 *
 * <p>Claims emitidos: {@code sub} = correo, {@code uid}, {@code run}, {@code rol}, {@code iss}.</p>
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;
    private final String emisor;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.expiration-ms}") long expiracionMs,
                      @Value("${app.jwt.issuer:ferreteria-maestros}") String emisor) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret debe tener al menos 32 caracteres (256 bits) para firmar con HS256.");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.expiracionMs = expiracionMs;
        this.emisor = emisor;
    }

    /** Firma un token con los datos del usuario. */
    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .issuer(emisor)
                .claim("uid", usuario.getId())
                .claim("run", usuario.getRun())
                .claim("rol", usuario.getRol().getNombre())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusMillis(expiracionMs)))
                .signWith(clave)
                .compact();
    }

    /**
     * Valida la firma, el emisor y la expiración, y devuelve los claims.
     * @return claims si el token es válido; {@link Optional#empty()} si no lo es.
     */
    public Optional<Claims> extraerClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(clave)
                    .requireIssuer(emisor)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    /** Devuelve el correo ({@code sub}) del token si es válido. */
    public Optional<String> extraerCorreo(String token) {
        return extraerClaims(token).map(Claims::getSubject);
    }

    public long getExpiracionMs() {
        return expiracionMs;
    }
}
