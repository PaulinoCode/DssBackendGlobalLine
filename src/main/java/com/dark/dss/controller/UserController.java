package com.dark.dss.controller;

import com.dark.dss.entity.User;
import com.dark.dss.service.UserService;
import jakarta.validation.Valid; // Importante para validar datos
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Para saber quién está logueado
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        // Spring Security inyecta automáticamente la info del usuario logueado en 'authentication'
        String email = authentication.getName();

        // Buscamos sus datos completos en la BD para devolverlos al Frontend
        User user = userService.findAll().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // (Opcional) Por seguridad, borramos el password antes de enviarlo
        user.setPassword(null);

        return ResponseEntity.ok(user);
    }
    // ------------------------------------------------

    @GetMapping
    public List<User> getAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}