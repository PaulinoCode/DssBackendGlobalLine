package com.dark.dss.controller;

import com.dark.dss.entity.Product;
import com.dark.dss.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Listar todos
    @GetMapping
    public List<Product> getAll() {
        return productService.findAll();
    }

    // Ver por ID
    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return productService.findById(id);
    }

    // Ver por de un cliente
    @GetMapping("/client/{clientId}")
    public List<Product> getByClient(@PathVariable Long clientId) {
        return productService.findByClientId(clientId);
    }

    // Crear
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Product product) {
        try {
            return ResponseEntity.ok(productService.save(product));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Actualizar
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Product product) {
        try {
            return ResponseEntity.ok(productService.update(id, product));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}