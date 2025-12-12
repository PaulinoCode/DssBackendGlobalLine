# DSS Backend - Global Line
**Proyecto Académico - Sistemas de Apoyo para la Toma de Decisiones**

Sistema de Soporte a la Decisión (DSS) desarrollado con Spring Boot para la experiencia educativa "Sistemas de apoyo para la toma de decisiones". Implementa algoritmos de machine learning, análisis predictivo y generación de reportes empresariales.

## ✨ Características Principales

- **CRUD Completo**: Gestión de usuarios, clientes, productos y métricas
- **Dashboard Administrativo**: KPIs y gráficas de análisis empresarial
- **Machine Learning**: 
  - Predicción de ventas (Regresión OLS)
  - Análisis de riesgo (Simulación Montecarlo)
  - Correlación estadística (Pearson)
- **Carga Masiva**: Importación de métricas desde Excel
- **Reportes**: Generación de PDF y Excel con análisis avanzados

## 🛠️ Tecnologías

- **Java 25** + **Spring Boot 4.0**
- **PostgreSQL** + **Spring Data JPA**
- **Spring Security** (Autenticación/Autorización)
- **Smile ML** (Machine Learning)
- **Apache POI** (Excel) + **OpenPDF** (PDF)
- **Swagger/OpenAPI 3.0** (Documentación)

## 🚀 Instalación

### Prerrequisitos
- JDK 25+, Gradle 8.x, PostgreSQL

### Pasos
1. **Clona el proyecto**:
   ```bash
   git clone https://github.com/PaulinoCode/DssBackendGlobalLine
   cd DssBackendGlobalLine
   ```

2. **Configura PostgreSQL** en `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/dss_db
   spring.datasource.username=postgres
   spring.datasource.password=root
   ```

3. **Ejecuta**:
   ```bash
   ./gradlew bootRun
   ```

## 📖 Documentación API

**Swagger UI**: http://localhost:8080/docs

La API incluye 33 endpoints organizados en 7 controladores:
- **Usuarios** (5) - Gestión con encriptación automática
- **Clientes** (5) - CRUD empresarial  
- **Productos** (6) - Validación ASIN único
- **Métricas** (7) - CRUD + carga Excel
- **Dashboard** (4) - KPIs y gráficas
- **Predicciones** (3) - ML (OLS, Montecarlo, Correlación)
- **Reportes** (3) - PDF/Excel con análisis

## 🎓 Objetivos Académicos

Demuestra la implementación de conceptos DSS:
1. **Análisis Descriptivo**: Dashboard con KPIs
2. **Análisis Predictivo**: ML con regresión lineal
3. **Análisis Prescriptivo**: Correlaciones y recomendaciones
4. **Evaluación de Riesgo**: Simulación de escenarios
5. **Gestión de Datos**: ETL y validaciones
6. **Documentación Profesional**: API completamente documentada

## 📋 Análisis de Crash (Referencia)

Este repositorio incluye documentación de análisis de un crash de IntelliJ IDEA GitHub Copilot plugin para propósitos de referencia:

- **[CRASH_SUMMARY.md](CRASH_SUMMARY.md)** - Resumen ejecutivo del análisis
- **[COPILOT_CRASH_ANALYSIS.md](COPILOT_CRASH_ANALYSIS.md)** - Análisis técnico detallado
- **[ACTION_ITEMS.md](ACTION_ITEMS.md)** - Plan de acción para resolución

> **Nota**: Esta documentación analiza un crash del plugin de Copilot para IntelliJ IDEA (código Kotlin), no del código de este repositorio (Java Spring Boot). Se incluye como material de referencia sobre análisis de crashes y debugging de race conditions en sistemas concurrentes.

---
*Proyecto para demostrar la comprensión de Sistemas de Apoyo a la Decisión utilizando tecnologías empresariales modernas.*
