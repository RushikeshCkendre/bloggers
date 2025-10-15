package io.mountblue.blogger.service;

import io.mountblue.blogger.model.User;
import io.mountblue.blogger.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getUserByUsername(String username){
        return userRepository.getByUsername(username);
    }
}
