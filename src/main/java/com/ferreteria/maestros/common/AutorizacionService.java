package com.ferreteria.maestros.common;

import org.springframework.stereotype.Component;

/**
 * Puente para usar las reglas de {@link SecurityUtils} desde las expresiones SpEL de
 * {@code @PreAuthorize}. Permite escribir anotaciones legibles, por ejemplo:
 *
 * <pre>{@code
 * @PreAuthorize("@autorizacion.esAdmin()")
 * @PreAuthorize("@autorizacion.esAdminODuenio(#run)")
 * }</pre>
 *
 * <p>Se registra como bean con el nombre {@code autorizacion}.</p>
 */
@Component("autorizacion")
public class AutorizacionService {

    public boolean esAdmin() {
        return SecurityUtils.esAdmin();
    }

    public boolean esAdminODuenio(String run) {
        return SecurityUtils.esAdminODuenio(run);
    }
}
