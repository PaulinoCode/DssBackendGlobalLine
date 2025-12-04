
// java
package com.dark.dss.service;

import com.dark.dss.entity.Metric;
import com.dark.dss.entity.Product;
import com.dark.dss.repository.MetricRepository;
import com.dark.dss.repository.ProductRepository;
import org.springframework.stereotype.Service;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.vector.DoubleVector;
import smile.regression.OLS;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

@Service
public class PredictionService {

    private final MetricRepository metricRepository;
    private final ProductRepository productRepository;

    public PredictionService(MetricRepository metricRepository, ProductRepository productRepository) {
        this.metricRepository = metricRepository;
        this.productRepository = productRepository;
    }

    // RF-07: Predicción de Ventas (Machine Learning con Smile)
    public Map<String, Object> predictSales(Long productId, Double futureAdSpend) {
        List<Metric> history = metricRepository.findByProductIdOrderByDateAsc(productId);

        if (history.size() < 2) {
            throw new RuntimeException("Se necesitan al menos 2 registros históricos para predecir.");
        }

        // Preparar datos para Smile en un DataFrame
        double[] adSpend = history.stream().mapToDouble(Metric::getAdSpend).toArray();
        double[] salesUnits = history.stream().mapToDouble(Metric::getSalesUnits).toArray();

        DataFrame df = DataFrame.of(
                DoubleVector.of("AdSpend", adSpend),
                DoubleVector.of("SalesUnits", salesUnits)
        );

        try {
            // Entrenar Modelo
            Formula formula = Formula.of("SalesUnits", "AdSpend");
            var model = OLS.fit(formula, df);

            // Predecir usando el metodo que acepta el vector de predictores
            double predictedSales = model.predict(new double[]{futureAdSpend});

            Map<String, Object> result = new HashMap<>();
            result.put("predicted_units", (int) Math.round(predictedSales));
            result.put("future_ad_spend", futureAdSpend);
            result.put("model_accuracy", model.RSquared());
            result.put("model", "OLS-Smile");

            return result;
        } catch (NoClassDefFoundError ncde) {
            throw new RuntimeException("Falta la librería nativa requerida por Smile (OpenBLAS). Añade `implementation(\"org.bytedeco:openblas-platform:0.3.21-1.5.8\")` en `build.gradle.kts` y reconstruye.", ncde);
        } catch (Throwable t) {
            throw new RuntimeException("Error al entrenar/predict con Smile: " + t.getMessage(), t);
        }
    }

    // RF-08: Simulación de Montecarlo
    public Map<String, Object> analyzeRisk(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        double basePrice = product.getPrice();
        double baseCost = product.getCost();

        int iterations = 1000;
        int profitableScenarios = 0;
        int lossScenarios = 0;
        Random random = new Random();

        for (int i = 0; i < iterations; i++) {
            // Variación aleatoria del ±15%
            double priceVariation = 0.85 + (1.15 - 0.85) * random.nextDouble();
            double costVariation = 0.85 + (1.15 - 0.85) * random.nextDouble();

            double simulatedPrice = basePrice * priceVariation;
            double simulatedCost = baseCost * costVariation;

            if ((simulatedPrice - simulatedCost) > 0) {
                profitableScenarios++;
            } else {
                lossScenarios++;
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("profitable_scenarios", profitableScenarios);
        response.put("loss_scenarios", lossScenarios);
        response.put("total_simulations", iterations);

        return response;
    }

    /**
     * RF-Extra: Análisis de Correlación para medir el impacto de la publicidad.
     */
    public Map<String, Object> calculateCorrelation(Long productId) {
        List<Metric> history = metricRepository.findByProductIdOrderByDateAsc(productId);

        if (history.size() < 2) {
            throw new RuntimeException("Se necesitan al menos 2 registros históricos para calcular correlación.");
        }

        // Convertimos a arrays para la librería Smile
        double[] adSpend = history.stream().mapToDouble(Metric::getAdSpend).toArray();
        double[] salesUnits = history.stream().mapToDouble(Metric::getSalesUnits).toArray();

        // Fórmula de Pearson (r) usando Smile
        double r = smile.math.MathEx.cor(adSpend, salesUnits);

        // Interpretación detallada del coeficiente
        String interpretation;

        if (r >= 0.7) {
            interpretation = "Inversión Altamente Eficiente. (A más publicidad, muchas más ventas)";
        } else if (r >= 0.3) {
            interpretation = "Impacto Moderado. (La publicidad ayuda, pero no es el único factor)";
        } else if (r > -0.3) {
            // Entre -0.3 y 0.3 se considera "sin correlación"
            interpretation = "Gasto Desperdiciado. (No hay relación clara entre gasto y ventas)";
        } else if (r > -0.7) {
            // Entre -0.7 y -0.3
            interpretation = "Relación Inversa Moderada. (Cuidado: al aumentar gasto, las ventas bajan levemente)";
        } else {
            // Menor a -0.7
            interpretation = "Relación Inversa Crítica. (Alerta: La publicidad está afectando negativamente las ventas)";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("correlation_coefficient", r);
        result.put("interpretation", interpretation);

        return result;
    }
}