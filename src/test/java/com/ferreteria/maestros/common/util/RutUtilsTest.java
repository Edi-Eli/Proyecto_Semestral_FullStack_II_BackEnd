package com.ferreteria.maestros.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pruebas unitarias de {@link RutUtils}. Incluye los RUN de los usuarios demo del seed.
 */
class RutUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {"195436789", "123456785", "204567891", "167895433", "111111111",
            "19543678-9", "19.543.678-9", " 19543678-9 "})
    void aceptaLosRunDeLosUsuariosDemoYConFormato(String run) {
        assertThat(RutUtils.esValido(run)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"19543678K", "123456789", "195436780", "12345678A", "abcdefgh9", "", "123"})
    void rechazaRunInvalidos(String run) {
        assertThat(RutUtils.esValido(run)).isFalse();
    }

    @Test
    void normalizaQuitandoPuntosGuionesYEspacios() {
        assertThat(RutUtils.normalizar(" 19.543.678-9 ")).isEqualTo("195436789");
        assertThat(RutUtils.normalizar(null)).isNull();
    }
}
