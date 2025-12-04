package com.dark.dss.controller;

import com.dark.dss.service.PredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/predict")
@CrossOrigin(origins = "*")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    /**
     * Endpoint para Predicción de Ventas
     * URL: POST http://localhost:8080/api/predict/sales/{id_producto}?adSpend={monto}
     */
    @PostMapping("/sales/{productId}")
    public ResponseEntity<Map<String, Object>> predictSales(
            @PathVariable Long productId,
            @RequestParam Double adSpend) {

        try {
            Map<String, Object> result = predictionService.predictSales(productId, adSpend);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para Análisis de Riesgo (Montecarlo)
     * URL: GET http://localhost:8080/api/predict/risk/{id_producto}
     */
    @GetMapping("/risk/{productId}")
    public ResponseEntity<Map<String, Object>> analyzeRisk(@PathVariable Long productId) {

        try {
            Map<String, Object> result = predictionService.analyzeRisk(productId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Endpoint para Análisis de Correlación
     * URL: GET http://localhost:8080/api/predict/correlation/{id_producto}
     */
    @GetMapping("/correlation/{productId}")
    public ResponseEntity<Map<String, Object>> getCorrelation(@PathVariable Long productId) {
        try {
            Map<String, Object> result = predictionService.calculateCorrelation(productId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}