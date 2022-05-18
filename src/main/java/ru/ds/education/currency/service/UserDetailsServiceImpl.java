package ru.ds.education.currency.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.ds.education.currency.model.Role;
import ru.ds.education.currency.repository.UserRepo;

import java.util.Optional;
import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepo userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepo userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final Optional<ru.ds.education.currency.model.User> user
                = userRepository.findByUsername(username);
        if (user.isPresent()) {
            final String dbUsername = user.get().getUsername();
            final String dbPassword = user.get().getPassword();
            final Set<Role> roles = user.get().getRoles();
            return new User(dbUsername, dbPassword, roles);
        } else {
            throw new UsernameNotFoundException("not correct username");
        }
    }

}
