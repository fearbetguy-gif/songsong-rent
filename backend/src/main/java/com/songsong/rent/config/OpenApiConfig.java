package com.songsong.rent.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rentOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("松松租房平台接口文档")
                        .description("租房平台后端基础接口文档")
                        .version("v1.0.0")
                        .contact(new Contact().name("songsong-rent"))
                        .license(new License().name("Apache 2.0")));
    }
}
