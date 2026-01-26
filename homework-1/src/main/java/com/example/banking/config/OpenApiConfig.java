package com.example.banking.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Banking Transactions API",
                version = "v1",
                description = "Simple banking API for homework-1"
        )
)
@Configuration
public class OpenApiConfig {
}
