package com.dark.dss.controller;

import com.dark.dss.service.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/predict")
@CrossOrigin(origins = "*")
@Tag(name = "Predicciones y Análisis", description = "API para análisis predictivo, evaluación de riesgos y correlaciones")
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
    @Operation(summary = "Predecir ventas futuras",
               description = "Utiliza regresión lineal OLS (Ordinary Least Squares) con la librería Smile para predecir ventas basándose en inversión publicitaria. " +
                          "Requiere al menos 2 registros históricos del producto. Retorna unidades predichas, precisión del modelo (R²) y otros datos relevantes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Predicción realizada exitosamente con datos del modelo"),
            @ApiResponse(responseCode = "400", description = "Datos insuficientes (mínimo 2 registros históricos) o error en el modelo"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Map<String, Object>> predictSales(
            @Parameter(description = "ID del producto a analizar") @PathVariable Long productId,
            @Parameter(description = "Monto de inversión publicitaria propuesta") @RequestParam Double adSpend) {

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
    @Operation(summary = "Analizar riesgo financiero",
               description = "Realiza una simulación de Montecarlo con 10,000 iteraciones para evaluar la probabilidad de éxito/pérdida. " +
                          "Simula variaciones del ±15% en precio y costos para generar diferentes escenarios financieros")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Análisis de riesgo completado con estadísticas de escenarios"),
            @ApiResponse(responseCode = "400", description = "Error en el análisis o configuración del producto"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Map<String, Object>> analyzeRisk(@Parameter(description = "ID del producto a analizar") @PathVariable Long productId) {

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
    @Operation(summary = "Calcular correlación de Pearson",
               description = "Calcula el coeficiente de correlación de Pearson entre inversión publicitaria y ventas usando Smile. " +
                          "Proporciona interpretación automática del impacto: Altamente Eficiente (r≥0.7), Moderado (0.3≤r<0.7), " +
                          "Desperdiciado (-0.3<r<0.3), o Inverso (r≤-0.3). Requiere mínimo 2 registros históricos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Correlaciones calculadas con interpretación automática"),
            @ApiResponse(responseCode = "400", description = "Datos insuficientes para el cálculo"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Map<String, Object>> getCorrelation(@Parameter(description = "ID del producto a analizar") @PathVariable Long productId) {
        try {
            Map<String, Object> result = predictionService.calculateCorrelation(productId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}