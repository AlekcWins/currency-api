package ru.ds.education.currency.util;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.ds.education.currency.model.Role;
import ru.ds.education.currency.model.User;
import ru.ds.education.currency.repository.UserRepo;

import java.util.Collections;

@Service
@AllArgsConstructor
public class UserCreator {
    private final PasswordEncoder passwordEncoder;
    private UserRepo userRepository;

    private void createIfNotExist(String username, String password) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setRoles(Collections.singleton(Role.USER));
            user.setPassword(passwordEncoder.encode(password));
            user.setUsername(username);
            userRepository.save(user);
        }
    }

    public void createUserForTest() {
        createIfNotExist("admin", "admin_pass");
    }
}
