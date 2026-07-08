package edgareldy.springsecuritytutorial.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the Swagger UI metadata (title, description, version) and the
 * JWT bearer security scheme, so the "Authorize" button in Swagger UI can
 * attach a token to protected requests once feature/auth issues JWTs.
 * <p>
 * Created by edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-security-tutorial
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI springSecurityTutorialOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Security Tutorial API")
                        .description("Authentication and authorization API demonstrating the Spring Security "
                                + "ecosystem: users, RBAC (roles and permissions), account activation, "
                                + "password reset, and JWT authentication.")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
