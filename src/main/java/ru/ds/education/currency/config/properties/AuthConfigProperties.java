package ru.ds.education.currency.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Component
@ConfigurationProperties(prefix = "auth")
@Validated
@Getter
@Setter
public class AuthConfigProperties {

    @NotEmpty
    private String user;

    @NotEmpty
    private String password;

}
