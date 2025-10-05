package com.example.finance.controller;

import com.example.finance.model.User;
import com.example.finance.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;

@Controller
public class AuthController {
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }
    
    @PostMapping("/registration")
    public String registerUser(
        @Valid User user,
        BindingResult bindingResult,
        @RequestParam(required = false) boolean isAdmin,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "registration";
        }
        
        if (!userService.registerUser(user, isAdmin)) {
            model.addAttribute("usernameError", "Пользователь с таким именем уже существует");
            return "registration";
        }
        
        return "redirect:/login";
    }
}
