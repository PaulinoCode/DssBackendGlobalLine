package com.dark.dss.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Metric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "sales_units", nullable = false)
    private Integer salesUnits; // Variable Y (Objetivo a predecir)

    @Column(name = "ad_spend", nullable = false)
    private Double adSpend; // Variable X (Gasto en publicidad)

    @Column(name = "revenue", nullable = false)
    private Double revenue; // Ventas totales en dinero

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
