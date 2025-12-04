package com.dark.dss.controller;

import com.dark.dss.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // Descargar PDF
    // GET http://localhost:8080/api/reports/prediction-pdf
    @GetMapping("/prediction-pdf")
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

    // Descargar Excel
    // GET http://localhost:8080/api/reports/metrics-excel
    @GetMapping("/metrics-excel")
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



    // GET http://localhost:8080/api/reports/risk-pdf/{productId}
    @GetMapping("/risk-pdf/{productId}")
    public ResponseEntity<byte[]> downloadRiskReport(@PathVariable Long productId) {
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