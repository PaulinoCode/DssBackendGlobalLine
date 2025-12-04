package com.dark.dss.security;

import com.dark.dss.entity.User;
import com.dark.dss.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Buscamos en TU base de datos
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // 2. Traducimos al formato de Spring Security
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()) // Tu email es el usuario
                .password(user.getPassword())  // Tu contraseña encriptada
                .roles(user.getRole())         // Tu rol (ADMIN, MANAGER)
                .build();
    }
}