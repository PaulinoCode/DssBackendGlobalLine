package com.dark.dss.repository;

import com.dark.dss.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    //Buscar productos que pertenecen al Cliente con el ID
    List<Product> findByClientId(Long clientId);

    // NUEVO: Buscar por código ASIN (Para evitar duplicados)
    Optional<Product> findByAsin(String asin);
}
