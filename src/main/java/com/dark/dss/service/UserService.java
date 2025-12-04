package com.dark.dss.service;

import com.dark.dss.entity.User;
import com.dark.dss.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // <--- Inyectamos el encriptador

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Listar todos
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // Buscar por ID
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("No encontrado"));
    }

    // Buscar por Email
    public User createUser(User user) {
        // Antes de guardar, encriptamos la contraseña
        String passwordEncriptada = passwordEncoder.encode(user.getPassword());
        user.setPassword(passwordEncriptada);
        return userRepository.save(user);
    }

    // Actualizar
    public User updateUser(Long id, User userDetails) {
        User user = findById(id);
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());

        // Solo encriptamos si la contraseña cambió y no está vacía
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        return userRepository.save(user);
    }

    // Eliminar
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}