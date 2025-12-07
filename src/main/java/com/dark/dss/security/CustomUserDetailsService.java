package com.dark.dss.security;

import com.dark.dss.entity.User;
import com.dark.dss.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servicio que actúa como el puente entre la base de datos de usuarios de la aplicación
 * y el sistema de autenticación de Spring Security.
 *
 * La única responsabilidad de esta clase es implementar el metodo {@link #loadUserByUsername(String)},
 * que Spring Security invoca automáticamente durante un intento de login.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Carga los datos de un usuario desde la base de datos basándose en su identificador único (en este caso, el email).
     * Spring Security llama a este metodo cuando un usuario intenta autenticarse.
     *
     * @param email El email (que actúa como nombre de usuario) que el usuario introdujo en el formulario de login.
     * @return Un objeto {@link UserDetails} que Spring Security utiliza para realizar la autenticación.
     *         Este objeto contiene el nombre de usuario, la contraseña (ya encriptada) y los roles del usuario.
     * @throws UsernameNotFoundException Si no se encuentra ningún usuario con el email proporcionado en la base de datos.
     *                                   Spring Security captura esta excepción y la interpreta como un fallo de autenticación.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Buscar al usuario en nuestra propia base de datos usando el repositorio.
        // El email es el identificador único que usamos para el login.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró un usuario con el email: " + email));

        // 2. "Traducir" nuestro objeto `User` (de com.dark.dss.entity) al objeto que Spring Security entiende (`UserDetails`).
        // Usamos un constructor "builder" para crear el objeto UserDetails de forma clara y legible.
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())      // El identificador principal que usa Spring Security.
                .password(user.getPassword())     // La contraseña YA ENCRIPTADA que está en la base de datos.
                .roles(user.getRole())            // Los roles del usuario (ej. "ADMIN", "MANAGER"). Spring les añadirá el prefijo "ROLE_".
                .build();
    }
}
