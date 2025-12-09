package com.dark.dss.repository;

import com.dark.dss.entity.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MetricRepository extends JpaRepository<Metric, Long> {
    // Buscar historial ordenado por fecha (importante para la predicción)
    List<Metric> findByProductIdOrderByDateAsc(Long productId);

    // OPTIMIZACIÓN: Buscar métricas para múltiples productos de una vez
    @Query("SELECT m FROM Metric m JOIN FETCH m.product WHERE m.product.id IN :productIds ORDER BY m.product.id ASC, m.date ASC")
    List<Metric> findByProductIdInOrderByProductIdAscDateAsc(@Param("productIds") List<Long> productIds);

    // OPTIMIZACIÓN: Buscar todas las métricas con producto y cliente cargados (evita N+1)
    @Query("SELECT m FROM Metric m JOIN FETCH m.product p LEFT JOIN FETCH p.client")
    List<Metric> findAllWithProduct();

    //ADMIN
    //KPI Global: Dinero total histórico
    @Query("SELECT SUM(m.revenue) FROM Metric m")
    Double getTotalRevenue();

    //Gráfica Global: Ventas agrupadas por mes (RF-03)
    // Devuelve: [Fecha, SumaTotal] (Ej: 2024-01-01, 50000.00)
    @Query("SELECT m.date, SUM(m.revenue) FROM Metric m GROUP BY m.date ORDER BY m.date ASC")
    List<Object[]> findGlobalMonthlyRevenue();

    //Gráfica Pie: Ventas por Cliente
    @Query("SELECT c.name, SUM(m.revenue) FROM Metric m JOIN m.product p JOIN p.client c GROUP BY c.name")
    List<Object[]> findRevenueByClient();

    //Gráfica Barras: Top 5 Global
    @Query("SELECT p.name, SUM(m.revenue) as total FROM Metric m JOIN m.product p GROUP BY p.name ORDER BY total DESC LIMIT 5")
    List<Object[]> findTop5Products();
}