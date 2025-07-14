package ua.azaika.serverpulse.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 @author Andrii Zaika
 **/
@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private static final String BEARER_AUTHENTICATION = "Bearer Authentication";
    @Value("${api.version}")
    private String apiVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ServerPulse API")
                        .description("ServerPulse API documentation")
                        .version(apiVersion))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTHENTICATION))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTHENTICATION, new SecurityScheme()
                                .name(BEARER_AUTHENTICATION)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("JWT token provided by the server pulse authentication service")
                        )
                );
    }
}
