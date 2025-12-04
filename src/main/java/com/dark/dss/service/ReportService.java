package com.dark.dss.service;

import com.dark.dss.entity.Metric;
import com.dark.dss.entity.Product;
import com.dark.dss.repository.MetricRepository;
import com.dark.dss.repository.ProductRepository;
import com.lowagie.text.*; // OpenPDF
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*; // Apache POI
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final ProductRepository productRepository;
    private final MetricRepository metricRepository;
    private final PredictionService predictionService;

    public ReportService(ProductRepository productRepository,
                         MetricRepository metricRepository,
                         PredictionService predictionService) {
        this.productRepository = productRepository;
        this.metricRepository = metricRepository;
        this.predictionService = predictionService;
    }

    /**
     * Generar PDF con Predicciones de Ventas
     * Simula una inversión estándar de $1,000 en publicidad para proyectar resultados.
     */
    public byte[] generatePredictionPdf() throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);

        document.open();

        // 1. Título del Reporte
        com.lowagie.text.Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Reporte de Predicciones - Global Line Solutions", fontTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" ")); // Espacio en blanco

        // 2. Tabla de Datos
        PdfPTable table = new PdfPTable(4); // 4 Columnas
        table.setWidthPercentage(100);

        // Encabezados
        addPdfHeader(table, "Producto");
        addPdfHeader(table, "Precio Actual");
        addPdfHeader(table, "Predicción (u) con $1000 Ads");
        addPdfHeader(table, "Precisión del Modelo (R²)");

        // Llenado de datos
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            table.addCell(product.getName());
            table.addCell("$" + product.getPrice());

            try {
                // Simulamos inversión de 1000 para estandarizar el reporte
                Map<String, Object> prediction = predictionService.predictSales(product.getId(), 1000.0);
                table.addCell(prediction.get("predicted_units").toString());

                // Formateamos el R2 a 2 decimales
                Double accuracy = (Double) prediction.get("model_accuracy");
                table.addCell(String.format("%.2f", accuracy));
            } catch (Exception e) {
                table.addCell("Sin datos históricos");
                table.addCell("-");
            }
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

    private void addPdfHeader(PdfPTable table, String text) {
        PdfPCell header = new PdfPCell();
        header.setPhrase(new Phrase(text));
        header.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(header);
    }

    /**
     * Generar Excel con todo el Histórico de Métricas
     */
    public byte[] generateMetricsExcel() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Histórico de Métricas");

            // Encabezados
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID Métrica", "Fecha", "Producto (ASIN)", "Nombre Producto", "Inversión Ads", "Unidades Vendidas", "Ingresos"};

            // Estilo negrita para encabezados
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenado de datos
            List<Metric> metrics = metricRepository.findAll();
            int rowIdx = 1;
            for (Metric metric : metrics) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(metric.getId());
                row.createCell(1).setCellValue(metric.getDate().toString());
                row.createCell(2).setCellValue(metric.getProduct().getAsin());
                row.createCell(3).setCellValue(metric.getProduct().getName());
                row.createCell(4).setCellValue(metric.getAdSpend());
                row.createCell(5).setCellValue(metric.getSalesUnits());
                row.createCell(6).setCellValue(metric.getRevenue());
            }

            // Ajustar ancho de columnas automático
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }


    // ... imports existentes ...
    // Asegúrate de importar tu servicio de predicción

    /**
     * Reporte PDF Detallado de Riesgo (Montecarlo) para un producto
     */
    public byte[] generateRiskPdf(Long productId) throws DocumentException {
        // 1. Obtener datos
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Map<String, Object> riskData = predictionService.analyzeRisk(productId);

        int profitable = (int) riskData.get("profitable_scenarios");
        int loss = (int) riskData.get("loss_scenarios");
        int total = (int) riskData.get("total_simulations");
        double successRate = (double) profitable / total * 100;

        // 2. Crear Documento PDF
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        // Título
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Análisis de Riesgo Financiero", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph("Producto: " + product.getName()));
        document.add(new Paragraph("ASIN: " + product.getAsin()));
        document.add(new Paragraph("Fecha de Análisis: " + java.time.LocalDate.now()));
        document.add(new Paragraph("--------------------------------------------------"));

        // Resultados
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        document.add(new Paragraph("\nResultados de la Simulación (Montecarlo)", subtitleFont));
        document.add(new Paragraph("Escenarios Simulados: " + total));
        document.add(new Paragraph("Escenarios con Ganancia: " + profitable));
        document.add(new Paragraph("Escenarios con Pérdida: " + loss));

        // Veredicto Visual
        Font verdictFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16,
                successRate > 70 ? java.awt.Color.GREEN.darker() : java.awt.Color.RED);

        document.add(new Paragraph("\nProbabilidad de Éxito: " + String.format("%.2f", successRate) + "%", verdictFont));

        if (successRate > 70) {
            document.add(new Paragraph("CONCLUSIÓN: Inversión Segura. El riesgo es bajo."));
        } else if (successRate > 40) {
            document.add(new Paragraph("CONCLUSIÓN: Riesgo Moderado. Proceder con cautela."));
        } else {
            document.add(new Paragraph("CONCLUSIÓN: Alto Riesgo. Se recomienda no invertir."));
        }

        document.close();
        return out.toByteArray();
    }



}
