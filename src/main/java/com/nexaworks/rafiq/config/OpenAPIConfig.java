package com.nexaworks.rafiq.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI rafiqOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rafiq API Documentation")
                        .description("API documentation for Rafiq Health Care Application")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Rafiq Support")
                                .email("support@rafiq.com")));
    }
}
