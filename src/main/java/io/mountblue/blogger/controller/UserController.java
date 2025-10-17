package io.mountblue.blogger.controller;

import io.mountblue.blogger.enums.Role;
import io.mountblue.blogger.model.User;
import io.mountblue.blogger.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(User user) {
        System.out.println("YO im here");
        User newUser = userService.save(user);

        if (newUser == null) {
            return "register";
        } else {
            return "redirect:/posts";
        }
    }

}
