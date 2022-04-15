package ru.ds.education.currency;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
//TODO remove or replace
//
//    public ApiInfo apiInfo(){
//        return  new ApiInfoBuilder()
//                .title("Currency api")
//                .description("Сервис currency api")
//                .license("License")
//                .licenseUrl("http://unlicense.org")
//                .termsOfServiceUrl("")
//                .version(getClass().getPackage().getImplementationVersion())
//                .build();
//    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(
                        new Info()
                                .title("Currency api")
                                .version(getClass()
                                        .getPackage()
                                        .getImplementationVersion()
                                )
                                .license(new License().name("Apache 2.0").url("http://springdoc.org"))
                );
    }
}
