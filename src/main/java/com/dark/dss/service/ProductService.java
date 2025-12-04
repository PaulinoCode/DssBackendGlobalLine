package com.dark.dss.service;

import com.dark.dss.entity.Product;
import com.dark.dss.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Listar todos
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    // Buscar por ID
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    // Buscar por Cliente
    public List<Product> findByClientId(Long clientId) {
        return productRepository.findByClientId(clientId);
    }

    // Guardar (Crear) con Validación
    public Product save(Product product) {
        // Validamos que el ASIN no exista ya
        Optional<Product> existing = productRepository.findByAsin(product.getAsin());
        if (existing.isPresent()) {
            throw new RuntimeException("El ASIN " + product.getAsin() + " ya está registrado.");
        }
        return productRepository.save(product);
    }

    // Actualizar
    public Product update(Long id, Product details) {
        Product product = findById(id);

        // Si cambian el ASIN, verificamos que no choque con otro
        if (!product.getAsin().equals(details.getAsin()) &&
                productRepository.findByAsin(details.getAsin()).isPresent()) {
            throw new RuntimeException("El ASIN " + details.getAsin() + " ya pertenece a otro producto.");
        }

        product.setAsin(details.getAsin());
        product.setName(details.getName());
        product.setPrice(details.getPrice());
        product.setCost(details.getCost());

        // Si mandan un cliente nuevo, lo actualizamos
        if (details.getClient() != null) {
            product.setClient(details.getClient());
        }

        return productRepository.save(product);
    }

    // Eliminar
    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}