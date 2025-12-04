package com.dark.dss.repository;

import com.dark.dss.entity.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import com.dark.dss.entity.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

import java.util.List;

public interface MetricRepository extends JpaRepository<Metric, Long> {
    // Buscar historial ordenado por fecha (importante para la predicción)
    List<Metric> findByProductIdOrderByDateAsc(Long productId);


    // --- ADMIN ---
    // 1. KPI Global: Dinero total histórico
    @Query("SELECT SUM(m.revenue) FROM Metric m")
    Double getTotalRevenue();

    // 2. Gráfica Global: Ventas agrupadas por mes (RF-03)
    // Devuelve: [Fecha, SumaTotal] (Ej: 2024-01-01, 50000.00)
    @Query("SELECT m.date, SUM(m.revenue) FROM Metric m GROUP BY m.date ORDER BY m.date ASC")
    List<Object[]> findGlobalMonthlyRevenue();

    // 3. Gráfica Pie: Ventas por Cliente
    @Query("SELECT c.name, SUM(m.revenue) FROM Metric m JOIN m.product p JOIN p.client c GROUP BY c.name")
    List<Object[]> findRevenueByClient();

    // 4. Gráfica Barras: Top 5 Global
    @Query("SELECT p.name, SUM(m.revenue) as total FROM Metric m JOIN m.product p GROUP BY p.name ORDER BY total DESC LIMIT 5")
    List<Object[]> findTop5Products();
}