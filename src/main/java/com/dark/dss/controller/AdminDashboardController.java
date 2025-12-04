package com.dark.dss.controller;

import com.dark.dss.repository.MetricRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard/admin")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    private final MetricRepository metricRepository;

    public AdminDashboardController(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    // KPI: Dinero Total (Tarjeta grande)
    @GetMapping("/kpi-total")
    public ResponseEntity<Map<String, Object>> getKpiTotal() {
        Double total = metricRepository.getTotalRevenue();
        return ResponseEntity.ok(Map.of("total_revenue", total != null ? total : 0.0));
    }

    // Gráfica de Línea Global (Mes a Mes de toda la empresa)
    @GetMapping("/monthly-revenue")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyRevenue() {
        List<Object[]> results = metricRepository.findGlobalMonthlyRevenue();
        return ResponseEntity.ok(formatChartData(results, "date", "revenue"));
    }

    // Gráfica de Pie (Ventas por Cliente)
    @GetMapping("/sales-by-client")
    public ResponseEntity<List<Map<String, Object>>> getSalesByClient() {
        List<Object[]> results = metricRepository.findRevenueByClient();
        return ResponseEntity.ok(formatChartData(results, "client", "value"));
    }

    // Gráfica de Barras (Top 5 Productos Global)
    @GetMapping("/top-products")
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