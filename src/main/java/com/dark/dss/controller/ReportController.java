package com.dark.dss.controller;

import com.dark.dss.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
@Tag(name = "Reportes y Descargas", description = "API para la generación y descarga de reportes en PDF y Excel")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // Descargar PDF con Predicciones de Ventas
    @GetMapping("/prediction-pdf")
    @Operation(summary = "Descargar reporte de predicciones en PDF",
               description = "Genera un reporte PDF completo con predicciones de ventas para todos los productos. " +
                          "Simula una inversión estándar de $1,000 en publicidad y muestra: producto, precio actual, " +
                          "unidades predichas y precisión del modelo (R²)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF generado exitosamente con predicciones de todos los productos"),
            @ApiResponse(responseCode = "500", description = "Error interno al generar el PDF")
    })
    public ResponseEntity<byte[]> downloadPdf() {
        try {
            byte[] pdfBytes = reportService.generatePredictionPdf();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=predicciones_global_line.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Descargar Excel con Histórico de Métricas
    @GetMapping("/metrics-excel")
    @Operation(summary = "Descargar histórico completo de métricas en Excel",
               description = "Genera un archivo Excel (.xlsx) con el histórico completo de todas las métricas. " +
                          "Incluye: ID métrica, fecha, ASIN del producto, nombre del producto, inversión publicitaria, " +
                          "unidades vendidas e ingresos. Formateado con encabezados en negrita y columnas autoajustadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Excel generado exitosamente con formato profesional"),
            @ApiResponse(responseCode = "500", description = "Error interno al generar el Excel")
    })
    public ResponseEntity<byte[]> downloadExcel() {
        try {
            byte[] excelBytes = reportService.generateMetricsExcel();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historico_metricas.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM) // Tipo genérico binario para Excel
                    .body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Descargar PDF con Análisis de Riesgo (Montecarlo) para un producto
    @GetMapping("/risk-pdf/{productId}")
    @Operation(summary = "Descargar reporte detallado de análisis de riesgo en PDF",
               description = "Genera un reporte PDF detallado del análisis de riesgo de Montecarlo para un producto específico. " +
                          "Incluye: información del producto, número de simulaciones, escenarios rentables/pérdida, " +
                          "probabilidad de éxito y conclusión automática con código de colores (Verde: seguro, Rojo: riesgoso)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF de análisis de riesgo generado con conclusiones automáticas"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno al generar el PDF")
    })
    public ResponseEntity<byte[]> downloadRiskReport(@Parameter(description = "ID del producto para el análisis de riesgo") @PathVariable Long productId) {
        try {
            byte[] pdfBytes = reportService.generateRiskPdf(productId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_riesgo_" + productId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}