package ca.gbc.comp3095.productservice.config;


import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Value("${product-service.version}")
    public String version;

    @Bean
    public OpenAPI productServiceAPI() {

        return new OpenAPI()
                .info(new Info().title("Product Service API - COMP3095")
                .description("This is the REST API documentation for Product Service")
                    .version(version)
                    .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Product Service Confluence Documentation")
                        .url("https://mycompany.ca/productservice/docs"));

    }


}
