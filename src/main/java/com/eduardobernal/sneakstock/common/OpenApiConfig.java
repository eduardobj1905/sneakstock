package com.eduardobernal.sneakstock.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentación de la API. Interfaz interactiva en /swagger-ui.html y especificación en /v3/api-docs.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI sneakStockOpenApi() {
        return new OpenAPI().info(new Info()
                .title("SneakStock API")
                .version("1.0")
                .description("Gestión de stock para una cadena de tiendas de zapatillas: consulta por talla "
                        + "y tienda, ventas, entradas por albarán y traspasos entre tiendas."));
    }
}
