// java
package com.dark.dss.config;

import com.dark.dss.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Necesario para que funcionen los POST desde Postman/Angular
                .authorizeHttpRequests(auth -> auth
                        // IMPORTANTE: Permitimos crear usuarios (POST) sin login inicial para que puedas registrarte
                        // OJO: En producción esto debería estar protegido, pero para el MVP facilita las cosas.
                        .requestMatchers("/api/users").permitAll()
                        // Cualquier otra cosa requiere login
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults()); // Usa autenticación básica (Popup del navegador o Auth Header)

        return http.build();
    }

    // El Encriptador Oficial
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // El Gerente de Autenticación (Conecta el Traductor con el Encriptador)
    @Bean
    public AuthenticationManager authenticationManager(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        // Usar el constructor que recibe el UserDetailsService (API actual de Spring Security)
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(List.of(authProvider));
    }
}