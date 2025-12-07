package com.dark.dss.service;

import com.dark.dss.entity.Metric;
import com.dark.dss.entity.Product;
import com.dark.dss.repository.MetricRepository;
import com.dark.dss.repository.ProductRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MetricService {

    private final MetricRepository metricRepository;
    private final ProductRepository productRepository;

    public MetricService(MetricRepository metricRepository, ProductRepository productRepository) {
        this.metricRepository = metricRepository;
        this.productRepository = productRepository;
    }

    // Listar todas (Admin)
    public List<Metric> findAll() {
        return metricRepository.findAll();
    }

    // Buscar historial de un producto (Manager/Gráficas)
    public List<Metric> findByProduct(Long productId) {
        return metricRepository.findByProductIdOrderByDateAsc(productId);
    }

    // Buscar por ID
    public Metric findById(Long id) {
        return metricRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Métrica no encontrada con ID: " + id));
    }

    // Guardar
    public Metric save(Metric metric) {
        if (metric.getProduct() == null || metric.getProduct().getId() == null) {
            throw new RuntimeException("Es necesario especificar el producto (product_id).");
        }

        // Verificamos existencia del producto para no romper la integridad referencial
        productRepository.findById(metric.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("El producto especificado no existe."));

        return metricRepository.save(metric);
    }

    // Actualizar
    public Metric update(Long id, Metric details) {
        Metric metric = findById(id);

        metric.setDate(details.getDate());
        metric.setSalesUnits(details.getSalesUnits());
        metric.setAdSpend(details.getAdSpend());
        metric.setRevenue(details.getRevenue());

        // Si mandan un producto nuevo, lo actualizamos
        if (details.getProduct() != null) {
            Product product = productRepository.findById(details.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("El producto especificado no existe."));
            metric.setProduct(product);
        }

        return metricRepository.save(metric);
    }

    // Eliminar
    public void delete(Long id) {
        metricRepository.deleteById(id);
    }

    // CARGA MASIVA
    public String saveMetricsFromExcel(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("El archivo Excel está vacío.");
        }

        List<Metric> metricsList = new ArrayList<>();
        int rowCount = 0;

        // Leer Excel
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Saltar encabezados

                // 1. ASIN
                String asin = getCellValueAsString(row.getCell(0));
                if (asin.trim().isEmpty()) continue;

                // Verificamos existencia del producto para no romper la integridad referencial
                // TODO: OPTIMIZAR BUSCA DE ASIN, GUARDAR TODOS Y DESPUES COMPARARLOS
                Product product = productRepository.findByAsin(asin)
                        .orElseThrow(() -> new RuntimeException("Fila " + (row.getRowNum() + 1) + ": No existe producto con ASIN " + asin));

                // 2. FECHA (Soporta múltiples formatos)
                LocalDate date;
                try {
                    date = parseExcelDate(row.getCell(1));
                } catch (Exception e) {
                    throw new RuntimeException("Error de fecha en fila " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }

                // 3. NÚMEROS
                try {
                    Integer salesUnits = (int) getCellValueAsDouble(row.getCell(2));
                    Double adSpend = getCellValueAsDouble(row.getCell(3));
                    Double revenue = getCellValueAsDouble(row.getCell(4));

                    Metric metric = new Metric();
                    metric.setProduct(product);
                    metric.setDate(date);
                    metric.setSalesUnits(salesUnits);
                    metric.setAdSpend(adSpend);
                    metric.setRevenue(revenue);

                    metricsList.add(metric);
                    rowCount++;
                } catch (Exception e) {
                    throw new RuntimeException("Error numérico en fila " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }
            }

            // Guardar todas las métricas
            metricRepository.saveAll(metricsList);
            return "Carga exitosa: Se procesaron " + rowCount + " métricas.";

        } catch (IOException e) {
            throw new RuntimeException("Error al leer archivo: " + e.getMessage());
        }
    }

    // HELPER DE FECHAS
    private LocalDate parseExcelDate(Cell cell) {
        if (cell == null) throw new RuntimeException("Celda de fecha vacía");

        // 1. Si es formato nativo de Excel (Fecha o Número Serial)
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            // Si es un número puro (ej. 45292)
            return DateUtil.getJavaDate(cell.getNumericCellValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        // 2. Si es Texto, probamos varios formatos
        String dateStr = getCellValueAsString(cell).trim();
        if (dateStr.isEmpty()) throw new RuntimeException("Fecha vacía");

        // Lista de formatos aceptados (Agrega más si necesitas)
        List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"), // 2024-01-31 (Estándar ISO)
                DateTimeFormatter.ofPattern("dd/MM/yyyy"), // 31/01/2024 (Latam/España)
                DateTimeFormatter.ofPattern("M/d/yyyy"),   // 1/31/2024 (USA)
                DateTimeFormatter.ofPattern("dd-MM-yyyy"), // 31-01-2024
                DateTimeFormatter.ofPattern("yyyy/MM/dd")  // 2024/01/31
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException ignored) {
                // Probamos el siguiente...
            }
        }

        // Si fallaron los formatos de texto, intentamos ver si es un número serial escrito como texto (ej "45292")
        if (dateStr.matches("-?\\d+(\\.\\d+)?")) {
            try {
                double serial = Double.parseDouble(dateStr);
                return DateUtil.getJavaDate(serial).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (Exception ignored) {}
        }

        throw new RuntimeException("Formato de fecha no reconocido: '" + dateStr + "'. Use dd/MM/yyyy o yyyy-MM-dd.");
    }

    // HELPER DE VALORES
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

    // HELPER DE VALORES
    private double getCellValueAsDouble(Cell cell) {
        if (cell == null) return 0.0;
        switch (cell.getCellType()) {
            case NUMERIC: return cell.getNumericCellValue();
            case STRING:
                String text = cell.getStringCellValue().trim().replace(",", ""); // Limpiar comas
                if (text.isEmpty()) return 0.0;
                try {
                    return Double.parseDouble(text);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Valor no numérico: " + text);
                }
            default: return 0.0;
        }
    }
}