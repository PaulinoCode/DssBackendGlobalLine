package com.dark.dss.config;

import com.dark.dss.entity.User;
import com.dark.dss.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Verificamos si ya existe el admin para no duplicarlo
            if (userRepository.findByEmail("admin@globalline.com").isEmpty()) {

                User admin = new User();
                admin.setEmail("admin@globalline.com");
                admin.setName("Administrador Principal");
                admin.setRole("ADMIN");
                // ¡Aquí la encriptamos automáticamente!
                admin.setPassword(passwordEncoder.encode("12345"));

                userRepository.save(admin);
                System.out.println("Usuario ADMIN creado automáticamente: admin@globalline.com / 12345");
            }

            // Creamos un manager de prueba también
            if (userRepository.findByEmail("manager@globalline.com").isEmpty()) {
                User manager = new User();
                manager.setEmail("manager@globalline.com");
                manager.setName("Juan Perez");
                manager.setRole("MANAGER");
                manager.setPassword(passwordEncoder.encode("12345"));

                userRepository.save(manager);
                System.out.println("Usuario MANAGER creado automáticamente: manager@globalline.com / 12345");
            }
        };
    }
}