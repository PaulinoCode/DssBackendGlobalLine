package com.dark.dss.controller;

import com.dark.dss.entity.Metric;
import com.dark.dss.service.MetricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin(origins = "*")
@Tag(name = "Métricas", description = "API para la gestión de métricas de productos y análisis de datos")
public class MetricController {

    private final MetricService metricService;

    public MetricController(MetricService metricService) {
        this.metricService = metricService;
    }

    // Listar todos
    @GetMapping
    @Operation(summary = "Listar todas las métricas", description = "Obtiene una lista de todas las métricas registradas")
    @ApiResponse(responseCode = "200", description = "Lista de métricas obtenida exitosamente")
    public List<Metric> getAll() {
        return metricService.findAll();
    }

    // Endpoint para Gráficas: Histórico de un producto
    @GetMapping("/product/{productId}")
    @Operation(summary = "Obtener métricas por producto", description = "Obtiene el historial de métricas para un producto específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas del producto obtenidas exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public List<Metric> getByProduct(@Parameter(description = "ID del producto") @PathVariable Long productId) {
        return metricService.findByProduct(productId);
    }

    // Ver por ID
    @GetMapping("/{id}")
    @Operation(summary = "Buscar métrica por ID", description = "Obtiene una métrica específica por su identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métrica encontrada"),
            @ApiResponse(responseCode = "404", description = "Métrica no encontrada")
    })
    public ResponseEntity<Metric> getById(@Parameter(description = "ID de la métrica") @PathVariable Long id) {
        return ResponseEntity.ok(metricService.findById(id));
    }

    // Crear
    @PostMapping
    @Operation(summary = "Crear nueva métrica", description = "Registra una nueva métrica en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métrica creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<?> create(@RequestBody Metric metric) {
        try {
            return ResponseEntity.ok(metricService.save(metric));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Actualizar
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar métrica", description = "Actualiza la información de una métrica existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métrica actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Métrica no encontrada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<?> update(@Parameter(description = "ID de la métrica") @PathVariable Long id, @RequestBody Metric metric) {
        try {
            return ResponseEntity.ok(metricService.update(id, metric));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Eliminar
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar métrica", description = "Elimina una métrica del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Métrica eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Métrica no encontrada")
    })
    public ResponseEntity<Void> delete(@Parameter(description = "ID de la métrica") @PathVariable Long id) {
        metricService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ENDPOINT DE CARGA MASIVA
    @PostMapping("/upload")
    @Operation(summary = "Cargar métricas desde Excel",
               description = "Permite la carga masiva de métricas desde un archivo Excel (.xlsx). " +
                          "Formato esperado: [ASIN, Fecha, Unidades Vendidas, Inversión Ads, Ingresos]. " +
                          "Soporta múltiples formatos de fecha (dd/MM/yyyy, yyyy-MM-dd, etc.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo procesado exitosamente. Retorna número de registros importados"),
            @ApiResponse(responseCode = "400", description = "Error en formato del archivo, ASIN no encontrado, o datos inválidos")
    })
    public ResponseEntity<?> uploadMetrics(@Parameter(description = "Archivo Excel (.xlsx) con las métricas") @RequestParam("file") MultipartFile file) {
        try {
            String message = metricService.saveMetricsFromExcel(file);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}