package code.uz.bankcard.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI bankCardOpenAPI() {
        // Info
        Info info = new Info()
                .title("Bank Card Management API")
                .description("REST API for managing bank cards and transactions")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Nodirjon Okilov")
                        .email("nodiroqilov86@gmail.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://springdoc.org"));

        // Security
        SecurityRequirement securityRequirement = new SecurityRequirement();
        securityRequirement.addList("bearerAuth");

        SecurityScheme securityScheme = new SecurityScheme()
                .name("bearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER);

        Components components = new Components();
        components.addSecuritySchemes("bearerAuth", securityScheme);

        // OpenAPI
        return new OpenAPI()
                .info(info)
                .externalDocs(new ExternalDocumentation()
                        .description("Project Repository")
                        .url("https://gitlab.com/PaatoM/bank_rest"))
                .components(components)
                .addSecurityItem(securityRequirement);
    }
}
