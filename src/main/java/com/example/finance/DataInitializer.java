package com.example.finance;

import com.example.finance.model.Category;
import com.example.finance.model.CategoryType;
import com.example.finance.model.Role;
import com.example.finance.model.User;
import com.example.finance.repository.CategoryRepository;
import com.example.finance.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class DataInitializer {
    
    @Bean
    public CommandLineRunner initData(CategoryRepository categoryRepository,
                                      UserRepository userRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            if (categoryRepository.count() == 0) {
                categoryRepository.save(new Category(1, "Еда", CategoryType.FOOD, false, null));
                categoryRepository.save(new Category(2, "Здоровье", CategoryType.HEALTH, false, null));
                categoryRepository.save(new Category(3, "Образование", CategoryType.EDUCATION, false, null));
                categoryRepository.save(new Category(4, "Транспорт", CategoryType.TRANSPORT, false, null));
                categoryRepository.save(new Category(5, "Развлечения", CategoryType.ENTERTAINMENT, false, null));
                categoryRepository.save(new Category(6, "Техника", CategoryType.TECHNIC, false, null));
                categoryRepository.save(new Category(7, "Зарплата", CategoryType.SALARY, false, null));
            }
            
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setEmail("admin@example.com");
                admin.setRoles(Set.of(Role.ADMIN));
                admin.setActive(true);
                userRepository.save(admin);
            }
        };
    }
}
