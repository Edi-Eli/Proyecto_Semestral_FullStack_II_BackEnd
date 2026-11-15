package com.ferreteria.maestros.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ferreteria.maestros.usuario.entity.Rol;
import com.ferreteria.maestros.usuario.entity.Usuario;
import io.jsonwebtoken.Claims;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de {@link JwtService} (no requieren base de datos).
 */
class JwtServiceTest {

    private static final String SECRETO = "clave-de-prueba-suficientemente-larga-para-hs256";

    private final JwtService jwtService = new JwtService(SECRETO, 3_600_000L, "ferreteria-test");

    @Test
    void generaYValidaUnTokenConLosDatosDelUsuario() {
        Usuario usuario = usuarioDePrueba(1L, "sebas@duoc.cl", "195436789", "Administrador");

        String token = jwtService.generarToken(usuario);

        assertThat(token).isNotBlank();
        Optional<Claims> claims = jwtService.extraerClaims(token);
        assertThat(claims).isPresent();
        assertThat(claims.get().getSubject()).isEqualTo("sebas@duoc.cl");
        assertThat(claims.get().getIssuer()).isEqualTo("ferreteria-test");
        assertThat(claims.get().get("rol", String.class)).isEqualTo("Administrador");
        assertThat(claims.get().get("uid", Number.class).longValue()).isEqualTo(1L);
        assertThat(claims.get().get("run", String.class)).isEqualTo("195436789");
        assertThat(jwtService.extraerCorreo(token)).contains("sebas@duoc.cl");
    }

    @Test
    void rechazaUnTokenFirmadoConOtraClave() {
        JwtService otroServicio = new JwtService(
                "otra-clave-de-prueba-suficientemente-larga-para-hs256", 3_600_000L, "ferreteria-test");
        String tokenAjeno = otroServicio.generarToken(usuarioDePrueba(2L, "otro@duoc.cl", "111111111", "Cliente"));

        assertThat(jwtService.extraerClaims(tokenAjeno)).isEmpty();
    }

    @Test
    void rechazaUnTokenExpirado() {
        JwtService servicioExpirado = new JwtService(SECRETO, -1000L, "ferreteria-test");
        String token = servicioExpirado.generarToken(usuarioDePrueba(3L, "expirado@duoc.cl", "111111111", "Cliente"));

        assertThat(servicioExpirado.extraerClaims(token)).isEmpty();
    }

    @Test
    void rechazaUnTokenDeOtroEmisor() {
        JwtService otroEmisor = new JwtService(SECRETO, 3_600_000L, "otro-emisor");
        String token = otroEmisor.generarToken(usuarioDePrueba(4L, "emisor@duoc.cl", "111111111", "Cliente"));

        assertThat(jwtService.extraerClaims(token)).isEmpty();
    }

    @Test
    void rechazaUnTokenMalFormado() {
        assertThat(jwtService.extraerClaims("esto.no.es.un.jwt")).isEmpty();
        assertThat(jwtService.extraerCorreo("")).isEmpty();
    }

    private Usuario usuarioDePrueba(Long id, String correo, String run, String nombreRol) {
        Rol rol = new Rol(nombreRol, "rol de prueba");
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setCorreo(correo);
        usuario.setRun(run);
        usuario.setRol(rol);
        return usuario;
    }
}
