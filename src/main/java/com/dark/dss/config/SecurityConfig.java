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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configuración central de Spring Security.
 * Define las reglas de autenticación, autorización, CORS y la gestión de contraseñas.
 */
@Configuration
@EnableWebSecurity // Activa la configuración de seguridad web de Spring.
public class SecurityConfig {

    /**
     * Define la cadena de filtros de seguridad que protege las peticiones HTTP.
     * Es el corazón de la configuración de seguridad, donde se establecen las reglas de acceso.
     *
     * @param http El objeto HttpSecurity para construir la configuración.
     * @return La cadena de filtros de seguridad construida.
     * @throws Exception Si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CONFIGURACIÓN DE CORS:
                // Integra la configuración de CORS definida en el bean corsConfigurationSource().
                // Esto es crucial para permitir que el frontend (ej. Angular en localhost:4200) se comunique con el backend.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. DESACTIVACIÓN DE CSRF (Cross-Site Request Forgery):
                // Para APIs REST sin estado, CSRF no es necesario y puede dar problemas con clientes como Postman o frontends SPA.
                .csrf(AbstractHttpConfigurer::disable)

                // 3. REGLAS DE AUTORIZACIÓN DE PETICIONES:
                .authorizeHttpRequests(auth -> auth
                        // Permite que CUALQUIERA (incluso sin login) haga peticiones a "/api/users".
                        // Esto es vital para permitir el registro de nuevos usuarios.
                        .requestMatchers("/api/users").permitAll()
                        // Para CUALQUIER OTRA petición, el usuario debe estar autenticado.
                        .anyRequest().authenticated()
                )

                // 4. MECANISMO DE AUTENTICACIÓN:
                // Activa la autenticación HTTP Basic. Esto hace que el navegador muestre un popup de login
                // o que los clientes (como Postman) puedan enviar un encabezado "Authorization: Basic ...".
                .httpBasic(withDefaults());

        return http.build();
    }

    /**
     * Define la configuración de CORS (Cross-Origin Resource Sharing).
     * Especifica qué orígenes (dominios), métodos y cabeceras están permitidos.
     *
     * @return La fuente de configuración de CORS.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos: Solo tu frontend de Angular puede hacer peticiones.
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));

        // Métodos HTTP permitidos: GET, POST, etc.
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Cabeceras permitidas: "Authorization" es esencial para enviar el token de login.
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Permite que el navegador envíe credenciales (como cookies o encabezados de autenticación).
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta configuración a TODAS las rutas de la aplicación ("/**").
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Define el encriptador de contraseñas que se usará en toda la aplicación.
     * BCrypt es el estándar de la industria: es lento a propósito y usa un "salt" aleatorio
     * para hacer que las contraseñas sean muy difíciles de descifrar.
     *
     * @return Una instancia del encriptador BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura el "Gerente de Autenticación", el cerebro que procesa un intento de login.
     * Conecta las tres piezas clave del puzzle de autenticación:
     * 1. El servicio que sabe cómo encontrar un usuario (CustomUserDetailsService).
     * 2. El encriptador que sabe cómo comparar contraseñas (PasswordEncoder).
     * 3. El proveedor que une a los dos anteriores (DaoAuthenticationProvider).
     *
     * @param userDetailsService Tu servicio personalizado que carga los datos del usuario desde la BD.
     * @param passwordEncoder El encriptador de contraseñas definido arriba.
     * @return El AuthenticationManager configurado.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        // DaoAuthenticationProvider es la implementación estándar de Spring para la autenticación basada en BD.
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        // Le decimos al proveedor qué encriptador debe usar para comparar la contraseña enviada con la guardada.
        authProvider.setPasswordEncoder(passwordEncoder);

        // El ProviderManager es un AuthenticationManager que puede gestionar múltiples proveedores (aunque aquí solo usamos uno).
        return new ProviderManager(List.of(authProvider));
    }
}
