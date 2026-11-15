package com.ferreteria.maestros.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentación OpenAPI/Swagger. Define el esquema de seguridad <b>Bearer JWT</b> para que
 * se pueda probar la API protegida desde {@code /swagger-ui.html}.
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_BEARER = "bearerAuth";

    @Bean
    public OpenAPI ferreteriaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ferretería Los Maestros — API")
                        .version("v1")
                        .description("API REST del backend (monolito Spring Boot) para la ferretería. "
                                + "Autenticación JWT: haga login en /api/auth/login y use el token con el botón Authorize."))
                .components(new Components()
                        .addSecuritySchemes(ESQUEMA_BEARER, new SecurityScheme()
                                .name(ESQUEMA_BEARER)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_BEARER));
    }
}
