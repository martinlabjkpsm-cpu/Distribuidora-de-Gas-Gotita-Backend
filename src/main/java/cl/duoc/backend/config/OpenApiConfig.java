package cl.duoc.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Swagger UI en /swagger-ui.html. El botón "Authorize" acepta el accessToken del login. */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "API Distribuidora de Gas Gotita",
        version = "0.1.0",
        description = "Backend del proyecto semestral DSY1104: catálogo, pedidos de gas, roles y login con JWT."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
