package com.example.finance.service;

import com.example.finance.model.Role;
import com.example.finance.model.User;
import com.example.finance.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Пользователь с именем: " + username + " не найден"));

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.getAuthorities()
        );
    }
    
    public boolean registerUser(User user, boolean isAdmin) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return false;
        }
        
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return false;
        }
        
        user.setActive(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(isAdmin ? Set.of(Role.ADMIN) : Set.of(Role.USER));
        
        userRepository.save(user);
        return true;
    }

    @Transactional
    public void updateUserActivity(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastActivity(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public boolean isUserOnline(User user, Duration onlineThreshold) {
        if (user.getLastActivity() == null) {
            return false;
        }
        return Duration.between(user.getLastActivity(), LocalDateTime.now()).compareTo(onlineThreshold) < 0;
    }
}
