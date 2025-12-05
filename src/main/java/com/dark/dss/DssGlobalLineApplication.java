package com.dark.dss;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "DSS Backend - Global Line API",
				version = "1.0.0",
				description = "Proyecto académico para la experiencia educativa 'Sistemas de apoyo para la toma de decisiones'. " +
						"Sistema de Soporte a la Decisión (DSS) que demuestra la implementación de algoritmos de machine learning, " +
						"análisis predictivo, gestión de métricas y generación de reportes para la toma de decisiones empresariales.",
				contact = @Contact(
						name = "Proyecto Académico DSS",
						email = "estudiante@universidad.edu.mx"
				),
				license = @License(
						name = "Proyecto Educativo - MIT License",
						url = "https://opensource.org/licenses/MIT"
				)
		),
		servers = {
				@Server(
						description = "Servidor de Desarrollo Local",
						url = "http://localhost:8080"
				),
				@Server(
						description = "Servidor de Pruebas Académico",
						url = "http://localhost:8080"
				)
		}
)
public class DssGlobalLineApplication {

	public static void main(String[] args) {
		SpringApplication.run(DssGlobalLineApplication.class, args);
	}

}
