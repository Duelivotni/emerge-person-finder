import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	// 1. Update Spring Boot Plugin to 3.x.x
	id("org.springframework.boot") version "3.2.5" // Using 3.2.5 as a stable recent version
	id("io.spring.dependency-management") version "1.1.4" // This might be auto-updated by Spring Boot plugin
	kotlin("jvm") version "1.9.23" // 2. Update Kotlin version to 1.9.x for Spring Boot 3
	kotlin("plugin.spring") version "1.9.23"
	kotlin("plugin.jpa") version "1.9.23" // This plugin needs to support Jakarta as well
}

group = "com.persons.finder"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17 // Java 17 is minimum for Spring Boot 3.x

repositories {
	mavenCentral()
}

// Remove the configurations.all { resolutionStrategy { ... } } block entirely.
// With Spring Boot 3 and proper hibernate-spatial, it should no longer be needed.
// This is because hibernate-spatial for Hibernate 6 is built for new JTS versions.

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	// Database
	implementation("org.postgresql:postgresql")
	implementation("com.h2database:h2") // For testing

	// Spatial
	// 3. IMPORTANT: Change hibernate-spatial group ID for Hibernate 6 compatibility
	implementation("org.hibernate.orm:hibernate-spatial") // Spring Boot 3.x manages this version
	// Remove explicit jts-core dependency. It's handled transitively by hibernate-spatial.
	// implementation("org.locationtech.jts:jts-core:1.15.0") // <-- REMOVE THIS LINE
	implementation("org.n52.jackson:jackson-datatype-jts:1.2.10") // This might need an update later if it has Jakarta issues

	// API Docs
	// 4. Update springdoc-openapi-ui for Spring Boot 3 / Jakarta compatibility
	// implementation("org.springdoc:springdoc-openapi-ui:1.8.0") // Latest for Jakarta EE
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0") // Using 2.5.0 as the latest stable.

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17" // Ensure JVM target is 17
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}