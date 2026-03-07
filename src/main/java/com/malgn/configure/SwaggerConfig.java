package com.malgn.configure;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("(주)맑은기술 채용 과제 - CMS REST API")
                        .description("Simple CMS REST API Project")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("김승우")
                                .email("본인 이메일")
                                .url("https://github.com/tickling1/malgn-assignment")));
    }
}
