package com.dark.dss.controller;

import com.dark.dss.repository.MetricRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard/admin")
@CrossOrigin(origins = "*")
@Tag(name = "Dashboard Administrativo", description = "API para el panel de control administrativo con KPIs y gráficas")
public class AdminDashboardController {

    private final MetricRepository metricRepository;

    public AdminDashboardController(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    // KPI: Dinero Total (Tarjeta grande)
    @GetMapping("/kpi-total")
    @Operation(summary = "Obtener KPI total de ingresos", description = "Obtiene el total de ingresos de toda la empresa como indicador clave de rendimiento")
    @ApiResponse(responseCode = "200", description = "KPI de ingresos totales obtenido exitosamente")
    public ResponseEntity<Map<String, Object>> getKpiTotal() {
        Double total = metricRepository.getTotalRevenue();
        return ResponseEntity.ok(Map.of("total_revenue", total != null ? total : 0.0));
    }

    // Gráfica de Línea Global (Mes a Mes de toda la empresa)
    @GetMapping("/monthly-revenue")
    @Operation(summary = "Obtener ingresos mensuales", description = "Obtiene los datos de ingresos mes a mes para generar gráfica de línea temporal")
    @ApiResponse(responseCode = "200", description = "Datos de ingresos mensuales obtenidos exitosamente")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyRevenue() {
        List<Object[]> results = metricRepository.findGlobalMonthlyRevenue();
        return ResponseEntity.ok(formatChartData(results, "date", "revenue"));
    }

    // Gráfica de Pie (Ventas por Cliente)
    @GetMapping("/sales-by-client")
    @Operation(summary = "Obtener ventas por cliente", description = "Obtiene la distribución de ventas por cliente para generar gráfica de pastel")
    @ApiResponse(responseCode = "200", description = "Datos de ventas por cliente obtenidos exitosamente")
    public ResponseEntity<List<Map<String, Object>>> getSalesByClient() {
        List<Object[]> results = metricRepository.findRevenueByClient();
        return ResponseEntity.ok(formatChartData(results, "client", "value"));
    }

    // Gráfica de Barras (Top 5 Productos Global)
    @GetMapping("/top-products")
    @Operation(summary = "Obtener top 5 productos", description = "Obtiene los 5 productos más vendidos para generar gráfica de barras")
    @ApiResponse(responseCode = "200", description = "Datos de top productos obtenidos exitosamente")
    public ResponseEntity<List<Map<String, Object>>> getTopProducts() {
        List<Object[]> results = metricRepository.findTop5Products();
        return ResponseEntity.ok(formatChartData(results, "product", "sales"));
    }

    // Utilería para convertir los datos crudos de la BD en JSON bonito
    private List<Map<String, Object>> formatChartData(List<Object[]> data, String keyName, String valueName) {
        List<Map<String, Object>> formatted = new ArrayList<>();
        for (Object[] row : data) {
            Map<String, Object> item = new HashMap<>();
            item.put(keyName, row[0]);
            item.put(valueName, row[1]);
            formatted.add(item);
        }
        return formatted;
    }
}