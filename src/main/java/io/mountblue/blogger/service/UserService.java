package io.mountblue.blogger.service;

import io.mountblue.blogger.enums.Role;
import io.mountblue.blogger.model.User;
import io.mountblue.blogger.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private UserRepository userRepository;

    public User getUserByUsername(String username){
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        user.setRole(Role.AUTHOR);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
       return userRepository.save(user);
    }
}
