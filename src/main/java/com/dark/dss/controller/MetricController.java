package com.dark.dss.controller;

import com.dark.dss.entity.Metric;
import com.dark.dss.service.MetricService;
import jakarta.validation.Valid; // (Opcional, si agregaste @NotNull en tu Entity)
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin(origins = "*")
public class MetricController {

    private final MetricService metricService;

    public MetricController(MetricService metricService) {
        this.metricService = metricService;
    }

    @GetMapping
    public List<Metric> getAll() {
        return metricService.findAll();
    }

    // Endpoint para Gráficas: Histórico de un producto
    // GET /api/metrics/product/1
    @GetMapping("/product/{productId}")
    public List<Metric> getByProduct(@PathVariable Long productId) {
        return metricService.findByProduct(productId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Metric> getById(@PathVariable Long id) {
        return ResponseEntity.ok(metricService.findById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Metric metric) {
        try {
            return ResponseEntity.ok(metricService.save(metric));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Metric metric) {
        try {
            return ResponseEntity.ok(metricService.update(id, metric));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        metricService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- ENDPOINT DE CARGA MASIVA ---
    // POST http://localhost:8080/api/metrics/upload
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMetrics(@RequestParam("file") MultipartFile file) {
        try {
            String message = metricService.saveMetricsFromExcel(file);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}