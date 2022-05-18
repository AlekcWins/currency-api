package ru.ds.education.currency.runners;


import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.ds.education.currency.config.properties.AuthConfigProperties;
import ru.ds.education.currency.service.UserService;

@Component
@AllArgsConstructor
public class UserDataInitialization implements ApplicationRunner {

    private final UserService userService;

    private final AuthConfigProperties authConfigProperties;


    public void run(ApplicationArguments args) {
        String username = authConfigProperties.getUser();
        String password = authConfigProperties.getPassword();
        userService.createIfNotExist(username, password);
    }
}