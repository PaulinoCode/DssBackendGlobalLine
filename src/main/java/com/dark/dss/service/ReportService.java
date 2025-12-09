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
import java.util.*;

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
     * Generar PDF profesional con análisis completo de predicciones de ventas
     * Incluye análisis de correlación, proyecciones de ROI y recomendaciones estratégicas
     */
    public byte[] generatePredictionPdf() throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);

        document.open();

        // Estilos de fuente
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, java.awt.Color.DARK_GRAY);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, java.awt.Color.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, java.awt.Color.BLACK);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10, java.awt.Color.GRAY);

        // 1. ENCABEZADO DEL DOCUMENTO
        addDocumentHeader(document, titleFont, normalFont);

        // 2. RESUMEN EJECUTIVO
        addExecutiveSummary(document, subtitleFont, normalFont);

        // 3. ANÁLISIS DETALLADO POR PRODUCTO
        addDetailedProductAnalysis(document, subtitleFont, normalFont);

        // 4. CONCLUSIONES Y RECOMENDACIONES
        addConclusionsAndRecommendations(document, subtitleFont, normalFont);

        // 5. PIE DE PÁGINA
        addFooter(document, smallFont);

        document.close();
        return out.toByteArray();
    }

    private void addDocumentHeader(Document document, Font titleFont, Font normalFont) throws DocumentException {
        // Título principal
        Paragraph title = new Paragraph("REPORTE DE ANÁLISIS PREDICTIVO", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        // Subtítulo
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 16, java.awt.Color.BLUE);
        Paragraph subtitle = new Paragraph("Sistema de Apoyo para Toma de Decisiones", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        // INFORMACIÓN DESTACADA SOBRE LA INVERSIÓN
        Font investmentFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, java.awt.Color.RED);
        Paragraph investmentInfo = new Paragraph();
        investmentInfo.setAlignment(Element.ALIGN_CENTER);
        investmentInfo.add(new Chunk("💰 INVERSIÓN ESTÁNDAR SIMULADA: $1,000.00 MXN POR PRODUCTO 💰", investmentFont));

        // Marco alrededor de la información de inversión
        PdfPTable investmentTable = new PdfPTable(1);
        investmentTable.setWidthPercentage(80);
        PdfPCell investmentCell = new PdfPCell();
        investmentCell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        investmentCell.setBorderWidth(2);
        investmentCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        investmentCell.setPadding(10);

        Paragraph investmentText = new Paragraph();
        investmentText.add(new Chunk("📊 TODAS LAS PREDICCIONES SE BASAN EN UNA INVERSIÓN DE $1,000 MXN EN PUBLICIDAD POR PRODUCTO\n",
                                   FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, java.awt.Color.RED)));
        investmentText.add(new Chunk("Este monto es utilizado como estándar para comparar el rendimiento entre productos",
                                   FontFactory.getFont(FontFactory.HELVETICA, 11, java.awt.Color.BLACK)));
        investmentText.setAlignment(Element.ALIGN_CENTER);
        investmentCell.addElement(investmentText);
        investmentTable.addCell(investmentCell);
        investmentTable.setSpacingAfter(20);
        document.add(investmentTable);

        // Información del reporte
        Paragraph info = new Paragraph();
        info.add(new Chunk("Empresa: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        info.add(new Chunk("Global Line Solutions\n", normalFont));
        info.add(new Chunk("Fecha de Generación: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        info.add(new Chunk(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n", normalFont));
        info.add(new Chunk("Metodología: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        info.add(new Chunk("Regresión Lineal Múltiple (OLS) y Análisis de Correlación\n", normalFont));
        info.setSpacingAfter(20);
        document.add(info);
    }

    private void addExecutiveSummary(Document document, Font subtitleFont, Font normalFont) throws DocumentException {
        // Título de sección
        Paragraph sectionTitle = new Paragraph("RESUMEN EJECUTIVO", subtitleFont);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);

        // Calcular estadísticas generales y por cliente
        java.util.List<Product> products = productRepository.findAll();
        int totalProducts = products.size();
        int productsWithData = 0;
        double avgAccuracy = 0;
        double totalPotentialRevenue = 0;

        // Agrupar por cliente para estadísticas
        Map<String, java.util.List<Product>> productsByClient = new HashMap<>();
        for (Product product : products) {
            String clientName = product.getClient() != null ? product.getClient().getName() : "Sin Cliente Asignado";
            productsByClient.computeIfAbsent(clientName, k -> new ArrayList<>()).add(product);
        }

        // Calcular estadísticas globales
        for (Product product : products) {
            try {
                java.util.List<Metric> metrics = metricRepository.findByProductIdOrderByDateAsc(product.getId());
                if (metrics.size() >= 3) {
                    productsWithData++;
                    Map<String, Object> prediction = predictionService.predictSales(product.getId(), 1000.0);
                    avgAccuracy += (Double) prediction.get("model_accuracy");

                    int predictedUnits = (Integer) prediction.get("predicted_units");
                    totalPotentialRevenue += predictedUnits * product.getPrice();
                }
            } catch (Exception ignored) {}
        }

        if (productsWithData > 0) {
            avgAccuracy = avgAccuracy / productsWithData;
        }

        // Texto del resumen
        Paragraph summary = new Paragraph();
        summary.add(new Chunk("📈 ESTADÍSTICAS GENERALES\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, java.awt.Color.BLUE)));
        summary.add(new Chunk("🔍 Inversión estándar simulada: $1,000 MXN por producto\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, java.awt.Color.RED)));
        summary.add(new Chunk("• Total de clientes activos: ", normalFont));
        summary.add(new Chunk(String.valueOf(productsByClient.size()) + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        summary.add(new Chunk("• Total de productos analizados: ", normalFont));
        summary.add(new Chunk(String.valueOf(totalProducts) + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        summary.add(new Chunk("• Productos con datos suficientes para predicción: ", normalFont));
        summary.add(new Chunk(String.valueOf(productsWithData) + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        summary.add(new Chunk("• Precisión promedio del modelo: ", normalFont));
        summary.add(new Chunk(String.format("%.1f%%", avgAccuracy * 100) + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        summary.add(new Chunk("• Inversión total simulada en publicidad: ", normalFont));
        summary.add(new Chunk(String.format("$%.2f", productsWithData * 1000.0) + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, java.awt.Color.ORANGE.darker())));
        summary.add(new Chunk("• Potencial total de ingresos proyectados: ", normalFont));
        summary.add(new Chunk(String.format("$%.2f", totalPotentialRevenue) + "\n\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, java.awt.Color.GREEN.darker())));

        // Desglose por cliente
        summary.add(new Chunk("🏢 DESGLOSE POR CLIENTE (Inversión $1,000/producto)\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, java.awt.Color.BLUE)));

        for (Map.Entry<String, java.util.List<Product>> entry : productsByClient.entrySet()) {
            String clientName = entry.getKey();
            java.util.List<Product> clientProducts = entry.getValue();

            double clientRevenue = 0;
            int clientProductsWithData = 0;

            for (Product product : clientProducts) {
                try {
                    java.util.List<Metric> metrics = metricRepository.findByProductIdOrderByDateAsc(product.getId());
                    if (metrics.size() >= 3) {
                        clientProductsWithData++;
                        Map<String, Object> prediction = predictionService.predictSales(product.getId(), 1000.0);
                        int predictedUnits = (Integer) prediction.get("predicted_units");
                        clientRevenue += predictedUnits * product.getPrice();
                    }
                } catch (Exception ignored) {}
            }

            double clientInvestment = clientProductsWithData * 1000.0;
            summary.add(new Chunk("• " + clientName + ": ", normalFont));
            summary.add(new Chunk(clientProducts.size() + " productos", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
            summary.add(new Chunk(" | Inversión: ", normalFont));
            summary.add(new Chunk(String.format("$%.0f", clientInvestment), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, java.awt.Color.ORANGE.darker())));
            summary.add(new Chunk(" | Potencial: ", normalFont));
            summary.add(new Chunk(String.format("$%.2f", clientRevenue) + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, java.awt.Color.GREEN.darker())));
        }

        summary.setSpacingAfter(20);
        document.add(summary);
    }

    private void addDetailedProductAnalysis(Document document, Font subtitleFont, Font normalFont) throws DocumentException {
        // Título de sección
        Paragraph sectionTitle = new Paragraph("ANÁLISIS DETALLADO POR CLIENTE Y PRODUCTO", subtitleFont);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);

        java.util.List<Product> products = productRepository.findAll();

        // Agrupar productos por cliente
        Map<String, java.util.List<Product>> productsByClient = new HashMap<>();
        for (Product product : products) {
            String clientName = product.getClient() != null ? product.getClient().getName() : "Sin Cliente Asignado";
            productsByClient.computeIfAbsent(clientName, k -> new ArrayList<>()).add(product);
        }

        // Iterar por cada cliente
        for (Map.Entry<String, java.util.List<Product>> entry : productsByClient.entrySet()) {
            String clientName = entry.getKey();
            java.util.List<Product> clientProducts = entry.getValue();

            // Título del cliente
            Font clientTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, java.awt.Color.BLUE);
            Paragraph clientTitle = new Paragraph("📊 CLIENTE: " + clientName.toUpperCase(), clientTitleFont);
            clientTitle.setSpacingBefore(20);
            clientTitle.setSpacingAfter(10);
            document.add(clientTitle);

            // Calcular resumen del cliente
            addClientSummary(document, clientProducts, normalFont);

            // Tabla de productos del cliente
            PdfPTable table = new PdfPTable(7); // 7 columnas
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.0f, 1.2f, 1.5f, 1.3f, 1.3f, 1.5f, 2.2f});

            // Encabezados
            addPdfHeader(table, "Producto (ASIN)");
            addPdfHeader(table, "Precio");
            addPdfHeader(table, "Unidades Pred.\n(con $1,000 Ads)");
            addPdfHeader(table, "Ingresos Proj.\n(con $1,000 Ads)");
            addPdfHeader(table, "ROI %\n(Inversión $1,000)");
            addPdfHeader(table, "Precisión R²");
            addPdfHeader(table, "Recomendación");

            // Llenar datos de productos del cliente
            for (Product product : clientProducts) {
                table.addCell(new Paragraph(product.getName() + "\n(" + product.getAsin() + ")", FontFactory.getFont(FontFactory.HELVETICA, 9)));
                table.addCell("$" + String.format("%.2f", product.getPrice()));

                try {
                    Map<String, Object> prediction = predictionService.predictSales(product.getId(), 1000.0);
                    Map<String, Object> correlation = predictionService.calculateCorrelation(product.getId());

                    int predictedUnits = (Integer) prediction.get("predicted_units");
                    double accuracy = (Double) prediction.get("model_accuracy");
                    double projectedRevenue = predictedUnits * product.getPrice();
                    double roi = ((projectedRevenue - 1000) / 1000) * 100;
                    double correlationCoeff = (Double) correlation.get("correlation_coefficient");

                    table.addCell(String.valueOf(predictedUnits));
                    table.addCell("$" + String.format("%.2f", projectedRevenue));

                    // ROI con colores
                    PdfPCell roiCell = new PdfPCell(new Phrase(String.format("%.1f%%", roi)));
                    roiCell.setBackgroundColor(roi > 50 ? java.awt.Color.GREEN :
                                             roi > 0 ? java.awt.Color.YELLOW : java.awt.Color.PINK);
                    table.addCell(roiCell);

                    table.addCell(String.format("%.3f", accuracy));

                    // Recomendación basada en múltiples factores
                    String recommendation = generateRecommendation(roi, accuracy, correlationCoeff);
                    PdfPCell recCell = new PdfPCell(new Phrase(recommendation, FontFactory.getFont(FontFactory.HELVETICA, 8)));
                    table.addCell(recCell);

                } catch (Exception e) {
                    table.addCell("N/A");
                    table.addCell("N/A");
                    table.addCell("N/A");
                    table.addCell("N/A");
                    table.addCell("Sin datos históricos suficientes (mín. 3 registros)");
                }
            }

            document.add(table);
            document.add(new Paragraph(" ")); // Espacio entre clientes
        }
    }

    private void addClientSummary(Document document, java.util.List<Product> clientProducts, Font normalFont) throws DocumentException {
        double totalPotentialRevenue = 0;
        double totalInvestment = clientProducts.size() * 1000.0; // $1000 por producto
        int productsWithData = 0;
        double avgAccuracy = 0;

        for (Product product : clientProducts) {
            try {
                java.util.List<Metric> metrics = metricRepository.findByProductIdOrderByDateAsc(product.getId());
                if (metrics.size() >= 3) {
                    productsWithData++;
                    Map<String, Object> prediction = predictionService.predictSales(product.getId(), 1000.0);
                    avgAccuracy += (Double) prediction.get("model_accuracy");

                    int predictedUnits = (Integer) prediction.get("predicted_units");
                    totalPotentialRevenue += predictedUnits * product.getPrice();
                }
            } catch (Exception ignored) {}
        }

        if (productsWithData > 0) {
            avgAccuracy = avgAccuracy / productsWithData;
        }

        double totalROI = ((totalPotentialRevenue - totalInvestment) / totalInvestment) * 100;

        // Crear tabla de resumen del cliente
        PdfPTable summaryTable = new PdfPTable(5);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{1f, 1f, 1.2f, 1.2f, 1.2f});

        // Encabezados del resumen
        Font summaryHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.WHITE);
        PdfPCell headerCell1 = new PdfPCell(new Phrase("Total Productos", summaryHeaderFont));
        headerCell1.setBackgroundColor(java.awt.Color.DARK_GRAY);
        headerCell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryTable.addCell(headerCell1);

        PdfPCell headerCell2 = new PdfPCell(new Phrase("Con Datos", summaryHeaderFont));
        headerCell2.setBackgroundColor(java.awt.Color.DARK_GRAY);
        headerCell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryTable.addCell(headerCell2);

        PdfPCell headerCell3 = new PdfPCell(new Phrase("Inversión Total\n($1,000/producto)", summaryHeaderFont));
        headerCell3.setBackgroundColor(java.awt.Color.DARK_GRAY);
        headerCell3.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryTable.addCell(headerCell3);

        PdfPCell headerCell4 = new PdfPCell(new Phrase("ROI Proyectado", summaryHeaderFont));
        headerCell4.setBackgroundColor(java.awt.Color.DARK_GRAY);
        headerCell4.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryTable.addCell(headerCell4);

        PdfPCell headerCell5 = new PdfPCell(new Phrase("Precisión Promedio", summaryHeaderFont));
        headerCell5.setBackgroundColor(java.awt.Color.DARK_GRAY);
        headerCell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryTable.addCell(headerCell5);

        // Valores del resumen
        summaryTable.addCell(String.valueOf(clientProducts.size()));
        summaryTable.addCell(String.valueOf(productsWithData));

        // Inversión total del cliente
        PdfPCell investmentCell = new PdfPCell(new Phrase("$" + String.format("%.0f", totalInvestment)));
        investmentCell.setBackgroundColor(java.awt.Color.ORANGE);
        investmentCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryTable.addCell(investmentCell);

        PdfPCell roiCell = new PdfPCell(new Phrase(String.format("%.1f%%", totalROI)));
        roiCell.setBackgroundColor(totalROI > 50 ? java.awt.Color.GREEN :
                                  totalROI > 0 ? java.awt.Color.YELLOW : java.awt.Color.PINK);
        roiCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryTable.addCell(roiCell);

        summaryTable.addCell(String.format("%.1f%%", avgAccuracy * 100));

        summaryTable.setSpacingAfter(15);
        document.add(summaryTable);
    }

    private void addConclusionsAndRecommendations(Document document, Font subtitleFont, Font normalFont) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("CONCLUSIONES Y RECOMENDACIONES ESTRATÉGICAS", subtitleFont);
        sectionTitle.setSpacingBefore(20);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);

        Paragraph conclusions = new Paragraph();
        conclusions.add(new Chunk("Interpretación de Métricas:\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        conclusions.add(new Chunk("• ROI > 50%: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        conclusions.add(new Chunk("Inversión altamente rentable, escalar inmediatamente.\n", normalFont));
        conclusions.add(new Chunk("• ROI 0-50%: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        conclusions.add(new Chunk("Rentable pero moderada, monitorear de cerca.\n", normalFont));
        conclusions.add(new Chunk("• ROI < 0%: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        conclusions.add(new Chunk("Posible pérdida, revisar estrategia o pausar.\n", normalFont));
        conclusions.add(new Chunk("• R² > 0.7: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        conclusions.add(new Chunk("Modelo muy confiable para predicciones.\n", normalFont));
        conclusions.add(new Chunk("• R² < 0.3: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        conclusions.add(new Chunk("Baja confiabilidad, factores externos dominan.\n", normalFont));

        conclusions.setSpacingAfter(15);
        document.add(conclusions);

        Paragraph disclaimer = new Paragraph();
        disclaimer.add(new Chunk("NOTA IMPORTANTE: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, java.awt.Color.RED)));
        disclaimer.add(new Chunk("Este análisis se basa en datos históricos y utiliza modelos estadísticos. Los resultados reales pueden variar debido a factores del mercado, competencia, estacionalidad y otros eventos no previstos. Se recomienda usar este reporte como guía complementaria en la toma de decisiones.",
                               FontFactory.getFont(FontFactory.HELVETICA, 10, java.awt.Color.DARK_GRAY)));
        document.add(disclaimer);
    }

    private void addFooter(Document document, Font smallFont) throws DocumentException {
        Paragraph footer = new Paragraph();
        footer.setSpacingBefore(30);
        footer.add(new Chunk("───────────────────────────────────────────────────────────────────────\n", smallFont));
        footer.add(new Chunk("Global Line Solutions - Sistema DSS | Generado automáticamente\n", smallFont));
        footer.add(new Chunk("Para consultas técnicas contacte al administrador del sistema.", smallFont));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private String generateRecommendation(double roi, double accuracy, double correlation) {
        if (roi > 50 && accuracy > 0.7) {
            return "🟢 INVERTIR - Alta rentabilidad y confiabilidad";
        } else if (roi > 20 && accuracy > 0.5) {
            return "🟡 INVERTIR CON CAUTELA - Rentable pero monitorear";
        } else if (roi > 0 && accuracy > 0.3) {
            return "🟠 EVALUAR - Beneficio marginal, considerar otros productos";
        } else if (accuracy < 0.3) {
            return "⚪ DATOS INSUFICIENTES - Recopilar más histórico";
        } else {
            return "🔴 NO INVERTIR - Alto riesgo de pérdidas";
        }
    }

    private void addPdfHeader(PdfPTable table, String text) {
        PdfPCell header = new PdfPCell();
        header.setPhrase(new Phrase(text));
        header.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(header);
    }

    /**
     * Generar Excel el Histórico de Métricas
     */
    public byte[] generateMetricsExcel() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Histórico de Métricas");

            // 1. Encabezados en el nuevo orden
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID Métrica", "Nombre Producto", "Producto (ASIN)", "Fecha", "Unidades Vendidas", "Inversión Ads", "Ingresos"};

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

            // 2. Llenado de datos en el nuevo orden
            java.util.List<Metric> metrics = metricRepository.findAll();
            int rowIdx = 1;
            for (Metric metric : metrics) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(metric.getId());
                row.createCell(1).setCellValue(metric.getProduct().getName());
                row.createCell(2).setCellValue(metric.getProduct().getAsin());
                row.createCell(3).setCellValue(metric.getDate().toString());
                row.createCell(4).setCellValue(metric.getSalesUnits());
                row.createCell(5).setCellValue(metric.getAdSpend());
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

    // Reporte PDF Detallado de Riesgo (Montecarlo) para un producto
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
