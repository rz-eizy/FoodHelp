plugins {
	java
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
	// SonarQube
	id("org.sonarqube")
}

group = "com.example.foodhelp"
version = "0.0.1-SNAPSHOT"
description = "backendDeFoodhelp"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}


dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
sonar {
	properties {
		property("sonar.java.binaries",
			// Mantenemos la sintaxis simple y compatible:
			"${project.buildDir}/classes/java/main,${project.buildDir}/resources/main"
		)

		// Rutas de código fuente y pruebas
		property("sonar.sources", "src/main/java")
		property("sonar.tests", "src/test/java")
	}
}
tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named("sonar").configure {
	dependsOn(tasks.named("compileJava"), tasks.named("processResources"))
}