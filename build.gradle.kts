plugins {
	java
	id("org.springframework.boot") version "4.0.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.dark"
version = "0.0.1-SNAPSHOT"
description = "System for sales prediction and financial risk analysis."

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // 1. MACHINE LEARNING (Smile)
    implementation("com.github.haifengl:smile-core:3.0.2")
    // 2. REPORTES PDF (OpenPDF)
    implementation("com.github.librepdf:openpdf:1.3.30")
    // 3. REPORTES EXCEL (Apache POI)
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("org.bytedeco:openblas-platform:0.3.21-1.5.8")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
